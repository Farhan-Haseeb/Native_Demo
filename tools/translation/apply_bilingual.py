#!/usr/bin/env python3
"""Bilingual mode: keep every Chinese line and add its English (from tm.json) next to it.

Used to prepare a contribution for upstream, where the Chinese must stay. Never deletes or
changes an original character; it only inserts:

  * comment-only lines (`//`, `///`, ` * `, single-line `/* */`, `<!-- -->`, `#` in .pro):
    an English twin line is inserted directly below, same indent and comment marker
    (upstream's own `/// 中文` + `/// English` pattern);
  * everything else (string literals, log messages, trailing comments, XML text, Markdown,
    block-comment openers/closers, `@param` / `MARK:` lines): every text region that contains
    Chinese becomes `中文 / English` in place.

Skipped: keep_list.txt lines, lines that are already bilingual inline, Chinese comment lines
whose neighbour comment line is already their English twin, Chinese sections of block comments
that already carry an English copy, and lines without a tm.json entry (reported).

A region that contains a printf placeholder (%d, %@, %.1f, ...) is only duplicated when the
placeholder can be kept outside the duplicated part; otherwise the line is reported as manual,
because doubling a placeholder breaks String.format at runtime.

Usage: apply_bilingual.py [--dry-run] [--only GLOB] [--report FILE] [--overrides FILE]
       apply_bilingual.py --verify [--base REV]   # every change since REV is insert-only
Overrides: JSON {zh_stripped_line: bilingual_stripped_line} for lines fixed by hand
(also checked by --verify).
"""
import argparse
import difflib
import fnmatch
import json
import re
import subprocess
import sys

from common import CJK, REPO, TOOLS_DIR, lang_of, load_keep_list, load_tm, read_lines, scope_files, split_ws, tm_entry

SEP = ' / '
PROSE = re.compile(r'[A-Za-z]{2,}[^A-Za-z\n]+[A-Za-z]{2,}')
WORD = re.compile(r'[A-Za-z]{3,}')
HAN = re.compile(r'[㐀-䶿一-鿿豈-﫿]')
PRINTF = re.compile(r'%(\d+\$)?[-+ #0]*\d*(\.\d+)?(l{0,2}|h)[@dsfluxXcieEgG]|\{\}')
CODE_LANGS = ('kt', 'java', 'swift', 'objc', 'ets', 'gradle')
COMMENT_LANGS = CODE_LANGS + ('pbxproj',)
BLOCK = re.compile(r'/\*[\s\S]*?\*/')


# ---------------------------------------------------------------- region parsing

def regions(text, lang):
    """Split a line into text regions [(start, end)] (string contents, comment bodies, XML text)."""
    out = []
    i, n = 0, len(text)
    if lang in ('md', 'txt'):
        return [(0, n)]
    if lang in ('xml', 'plist'):
        if '<' not in text and re.search(r'[\w:]+\s*=\s*["\']', text):
            # attribute-only line of a multi-line tag: the quoted values are the text regions
            for m in re.finditer(r'"([^"]*)"|\'([^\']*)\'', text):
                g = 1 if m.group(1) is not None else 2
                out.append((m.start(g), m.end(g)))
            return out
        while i < n:
            if text.startswith('<!--', i):
                j = text.find('-->', i + 4)
                j = n if j < 0 else j
                out.append((i + 4, j))
                i = j + 3
            elif text[i] in '"\'':
                q = text[i]
                j = text.find(q, i + 1)
                j = n if j < 0 else j
                out.append((i + 1, j))
                i = j + 1
            elif text[i] == '<':
                j = text.find('>', i)
                if j < 0:
                    out.append((i, n))  # tag continues on the next line: treat the rest as text
                    break
                # attribute values inside the tag
                k = i
                while k < j:
                    if text[k] in '"\'':
                        q = text[k]
                        m = text.find(q, k + 1)
                        if m < 0 or m > j:
                            break
                        out.append((k + 1, m))
                        k = m + 1
                    else:
                        k += 1
                i = j + 1
            else:
                j = text.find('<', i)
                j = n if j < 0 else j
                out.append((i, j))
                i = j
        return out
    quotes = '"\'`' if lang == 'ets' else '"'
    if lang in COMMENT_LANGS and text.startswith('*') and not text.startswith('*/'):
        end = text.find('*/')
        return [(1, n if end < 0 else end)]  # a line inside a block comment is all prose
    while i < n:
        c = text[i]
        if c in quotes:
            j = i + 1
            while j < n and text[j] != c:
                if text.startswith('\\(', j) and lang == 'swift':
                    j = skip_balanced(text, j + 1, '(', ')')  # \( ... "nested" ... )
                elif text.startswith('${', j) and lang in ('kt', 'ets', 'gradle'):
                    j = skip_balanced(text, j + 1, '{', '}')
                else:
                    j += 2 if text[j] == '\\' else 1
            out.append((i + 1, min(j, n)))
            i = j + 1
        elif text.startswith('//', i) and lang in COMMENT_LANGS:
            out.append((i + 2, n))
            break
        elif text.startswith('/*', i) and lang in COMMENT_LANGS:
            j = text.find('*/', i + 2)
            if j < 0:
                out.append((i + 2, n))
                break
            out.append((i + 2, j))
            i = j + 2
        elif text.startswith('*/', i):
            out.append((0, i))  # closer of a block comment that opened on an earlier line
            i += 2
        elif c == '#' and lang in ('pro',):
            out.append((i + 1, n))
            break
        else:
            i += 1
    if not out:
        out = [(0, n)]  # bare prose (inside a block comment, multi-line string, ...)
    return out


def skip_balanced(text, k, op, cl):
    """Index just past the bracket that closes text[k] (== op), ignoring brackets inside quotes."""
    depth, n = 0, len(text)
    q = None
    while k < n:
        ch = text[k]
        if q:
            if ch == '\\':
                k += 1
            elif ch == q:
                q = None
        elif ch in '"\'':
            q = ch
        elif ch == op:
            depth += 1
        elif ch == cl:
            depth -= 1
            if depth == 0:
                return k + 1
        k += 1
    return n


def skeleton(text, regs):
    parts, pos = [], 0
    for a, b in regs:
        parts.append(text[pos:a])
        pos = b
    parts.append(text[pos:])
    return parts


def common_prefix_len(a, b):
    k = 0
    while k < len(a) and k < len(b) and a[k] == b[k]:
        k += 1
    return k


def merge_region(zh, en):
    """Return the bilingual region text for one zh/en region pair, or None if unsafe."""
    first = CJK.search(zh).start()
    last = max(m.end() for m in CJK.finditer(zh))
    # Keep shared text before the first / after the last Chinese char outside the duplicated part
    # (e.g. `心率: %d` + `Heart rate: %d` -> `心率 / Heart rate: %d`).
    p = min(common_prefix_len(zh, en), first)
    while p > 0 and zh[p - 1] not in ' :：(（[【"\'-_=,，':
        p -= 1  # only cut at a separator so words are not split
    rz, re_ = zh[::-1], en[::-1]
    s = min(common_prefix_len(rz, re_), len(zh) - last, len(en) - p)
    while s > 0 and zh[len(zh) - s] not in ' :：)）]】"\'.。!！?？,，;；-_=%$\\{':
        s -= 1
    if s and zh[len(zh) - s] not in ' :：)）]】"\'.。!！?？,，;；':
        s = 0  # suffix must start at a separator, not inside a placeholder or word
    def balance(t):
        return sum(t.count(c) for c in '(（[【「') - sum(t.count(c) for c in ')）]】」')
    # The shared tail must not close a bracket opened in the Chinese part (`血氧开关(查询) ...`):
    # move the cut right, past the closing bracket, to the next separator.
    while s > 0 and (balance(zh[p:len(zh) - s]) > 0 or zh[len(zh) - s] not in ' :：)）]】"\'.。!！?？,，;；'):
        s -= 1
    if balance(zh[p:len(zh) - s]) < 0:
        p = 0
    zmid, emid = zh[p:len(zh) - s], en[p:len(en) - s]
    if not emid.strip():
        return None
    if PRINTF.search(zmid) or PRINTF.search(emid):
        return None
    lead_ws = re.match(r'\s*', emid).group(0)
    return zh[:p] + zmid.rstrip() + SEP + emid[len(lead_ws):].rstrip() + zmid[len(zmid.rstrip()):] + zh[len(zh) - s:]


def inline(text, en, lang):
    """Bilingual line built region by region; None if the zh/en structure does not line up."""
    rz, ren = regions(text, lang), regions(en, lang)
    if len(rz) == len(ren) and skeleton(text, rz) == skeleton(en, ren):
        out, pos = [], 0
        for (a, b), (c, d) in zip(rz, ren):
            out.append(text[pos:a])
            seg = text[a:b]
            if CJK.search(seg):
                merged = merge_region(seg, en[c:d])
                if merged is None:
                    return None
                seg = merged
            out.append(seg)
            pos = b
        out.append(text[pos:])
        return ''.join(out)
    # fallback: one region spanning first..last Chinese char, shared prefix/suffix kept once
    runs = list(CJK.finditer(text))
    first, last = runs[0].start(), runs[-1].end()
    pre, suf = text[:first], text[last:]
    if en.startswith(pre) and en.endswith(suf) and len(en) > len(pre) + len(suf):
        mid = text[first:last]
        emid = en[len(pre):len(en) - len(suf)]
        if any(q in mid or q in emid for q in '"\'`') or PRINTF.search(mid + emid):
            return None
        return pre + mid + SEP + emid.strip() + suf
    return None


def already_bilingual(text, tm):
    """`中文 / English` already present: a slash after Chinese text, followed only by Latin text.

    A slash that is part of the content (`OkHttp / Okio`, `UI / 交互`) does not count: either Chinese
    follows it, or the English translation in tm.json contains the same slash phrase.
    """
    if text in tm:
        en, _ = tm_entry(tm[text])
        # Compare only the translated part (en minus the code before the first Chinese char, and
        # minus the shared tail) with the text from the first Chinese char on, case-sensitively,
        # so identifiers like `.cancel` or `week_mon` do not count as an existing translation.
        runs = list(CJK.finditer(text))
        first, last = runs[0].start(), runs[-1].end()
        mid = en[first:] if en.startswith(text[:first]) else en
        tail = text[last:]
        if tail and mid.endswith(tail):
            mid = mid[:-len(tail)]
        words = lambda t: set(re.findall(r'[A-Za-z]{2,}', t))
        want = words(mid)
        if len(want) >= 2 and want <= words(text[first:]):
            return True  # the English is already on the line ("中文\n\nEnglish", "中文 | English")
    for m in re.finditer(r'/|\\n', text):
        left, right = text[:m.start()], text[m.end():]
        if not CJK.search(left) or CJK.search(right) or not re.match(r'\s*[A-Za-z]', right):
            continue
        if left.endswith(('/', '*', '<')) or right.startswith(('/', '*', '>')):
            continue  # comment markers, not a separator
        if text in tm:
            en, _ = tm_entry(tm[text])
            phrase = (text[m.start():m.end()] + right).rstrip(' \'"`);,')
            if phrase and phrase in en:
                return False
        return True
    return False


# ---------------------------------------------------------------- comment twins

COMMENT_MARK = re.compile(r'^(///|//|\*(?!/)|<!--|#)\s?')


def twin_marker(text, lang):
    """Comment marker if the line is a comment-only line that can take a twin line below it."""
    if lang == 'xml':
        return '<!--' if text.startswith('<!--') and text.endswith('-->') and text.count('<!--') == 1 else None
    if lang == 'pro':
        return '#' if text.startswith('#') else None
    if lang not in CODE_LANGS:
        return None
    if text.startswith('//'):
        body = text.lstrip('/').strip()
        if body.startswith(('MARK', 'TODO', 'FIXME')) or text.count('"') % 2 or '//' in body:
            return None
        return '///' if text.startswith('///') else '//'
    if text.startswith('/*') and text.endswith('*/') and text.count('/*') == 1 and text.count('*/') == 1:
        return '/*'
    if text.startswith('*') and not text.startswith('*/') and '*/' not in text and '/*' not in text:
        if text.lstrip('*').strip().startswith('@'):
            return None
        return '*'
    return None


def adjacent_twin(lines, i, marker, en):
    """True when the neighbouring comment line already is the English of line i."""
    want = {w.lower() for w in WORD.findall(en or '')}
    lead = split_ws(lines[i])[0]
    for j in (i - 1, i + 1):
        if not 0 <= j < len(lines):
            continue
        l2, t2, _ = split_ws(lines[j])
        if l2 != lead or not t2.startswith(marker) or CJK.search(t2) or not PROSE.search(t2):
            continue
        if en is None:
            # no translation to compare with: accept an English prose line right after the Chinese one,
            # unless it looks like commented-out code
            if j == i + 1 and not re.match(r'^\W*\s*(add\(|case |if |let |var |val |return|\.|\w+\()', t2[len(marker):]):
                return True
            continue
        got = {w.lower() for w in WORD.findall(t2)}
        if want and len(want & got) / len(want | got) >= 0.3:
            return True
    return False


def bilingual_block_lines(lines, lang, ratio=0.7, min_lines=4):
    """Line indexes inside block comments whose Chinese section already has an English copy."""
    if lang not in CODE_LANGS:
        return set()
    text = ''.join(lines)
    starts = [0]
    for m in re.finditer('\n', text):
        starts.append(m.end())

    def line_at(pos):
        lo, hi = 0, len(starts) - 1
        while lo < hi:
            mid = (lo + hi + 1) // 2
            if starts[mid] <= pos:
                lo = mid
            else:
                hi = mid - 1
        return lo

    skip = set()
    for m in BLOCK.finditer(text):
        a, b = line_at(m.start()), line_at(m.end() - 1)
        block = lines[a:b + 1]
        cjk = [k for k, l in enumerate(block) if CJK.search(l)]
        if len(cjk) < min_lines:
            continue
        tail = block[max(cjk) + 1:]
        # English rows of a translated table can be short ("1: On"), so any Latin text counts
        prose = sum(1 for l in tail if re.search(r'[A-Za-z]{2,}', l.replace('*/', '')) and not re.match(r'^\s*\*?\s*@', l))
        if prose >= ratio * len(cjk):
            skip.update(a + k for k in cjk)
    return skip


# ---------------------------------------------------------------- apply

def load_overrides(path):
    p = path or (TOOLS_DIR / 'bilingual_overrides.json')
    try:
        with open(p, encoding='utf-8') as fh:
            return json.load(fh)
    except FileNotFoundError:
        return {}


def read_raw(rel):
    with open(REPO / rel, encoding='utf-8', newline='') as fh:  # keep CRLF files byte-identical
        return fh.read().splitlines(keepends=True)


def process(rel, tm, keep, overrides, stats):
    lines = read_raw(rel)
    lang = lang_of(rel)
    block_skip = bilingual_block_lines(lines, lang)
    out = []
    changed = False
    for i, line in enumerate(lines):
        out.append(line)
        if not HAN.search(line):
            continue  # nothing to translate (no Chinese, or only full-width punctuation)
        lead, text, trail = split_ws(line)
        loc = '%s:%d' % (rel, i + 1)
        if text in keep:
            stats['kept'] += 1
            continue
        if text in overrides:
            new = overrides[text]
            if new is None:  # explicitly left as-is
                stats['kept'] += 1
                continue
            out[-1] = lead + new + trail
            stats['override'] += 1
            changed = True
            continue
        if i in block_skip:
            stats['skip_bilingual_block'] += 1
            continue
        if already_bilingual(text, tm):
            stats['skip_bilingual_inline'].append(loc)
            continue
        if text not in tm:
            # a Chinese comment line whose next/previous line is already an English comment
            m = COMMENT_MARK.match(text)
            if m and lang in CODE_LANGS + ('xml', 'pro') and adjacent_twin(lines, i, m.group(1), None):
                stats['skip_existing_twin'] += 1
                continue
            stats['no_entry'].append({'loc': loc, 'zh': text})
            continue
        en, _force = tm_entry(tm[text])
        if not en.strip() or CJK.search(en):
            stats['manual'].append({'loc': loc, 'zh': text, 'en': en, 'why': 'bad tm entry'})
            continue
        marker = twin_marker(text, lang)
        if marker and en.startswith(marker.rstrip('*') or '*') and twin_marker(en, lang) == marker:
            if adjacent_twin(lines, i, marker, en):
                stats['skip_existing_twin'] += 1
                continue
            nl = trail if trail.endswith('\n') else '\n'
            if not trail.endswith('\n'):
                out[-1] = line + '\n'  # last line without newline: the twin goes after it
                nl = trail
            out.append(lead + en + nl)
            stats['twin'] += 1
            changed = True
            continue
        new = inline(text, en, lang)
        if new is None:
            stats['manual'].append({'loc': loc, 'zh': text, 'en': en, 'why': 'regions do not line up / placeholder'})
            continue
        out[-1] = lead + new + trail
        stats['inline'] += 1
        changed = True
    return out if changed else None


def run_apply(args):
    tm = load_tm(args.tm)
    keep = load_keep_list()
    overrides = load_overrides(args.overrides)
    stats = {'twin': 0, 'inline': 0, 'override': 0, 'kept': 0, 'skip_existing_twin': 0,
             'skip_bilingual_block': 0, 'skip_bilingual_inline': [], 'no_entry': [], 'manual': [],
             'files_changed': []}
    for rel in scope_files():
        if args.only and not fnmatch.fnmatch(rel, args.only):
            continue
        new = process(rel, tm, keep, overrides, stats)
        if new is None:
            continue
        stats['files_changed'].append(rel)
        if not args.dry_run:
            with open(REPO / rel, 'w', encoding='utf-8', newline='') as fh:
                fh.write(''.join(new))
    summary = {k: (len(v) if isinstance(v, list) else v) for k, v in stats.items()}
    summary['dry_run'] = args.dry_run
    print(json.dumps(summary, indent=1))
    for m in stats['manual'][:30]:
        print('MANUAL %s (%s)\n   zh: %s\n   en: %s' % (m['loc'], m['why'], m['zh'], m['en']))
    for m in stats['no_entry'][:30]:
        print('NO ENTRY %s: %s' % (m['loc'], m['zh']))
    if args.report:
        with open(args.report, 'w', encoding='utf-8') as fh:
            json.dump(stats, fh, ensure_ascii=False, indent=1)
    return 0


# ---------------------------------------------------------------- verify

def git_show(rev, rel):
    r = subprocess.run(['git', 'show', '%s:%s' % (rev, rel)], cwd=REPO, stdout=subprocess.PIPE,
                       stderr=subprocess.DEVNULL)
    return r.stdout.decode('utf-8') if r.returncode == 0 else None  # bytes as stored, CRLF kept


def insert_only(old, new):
    """Inserted chunks if `new` is `old` plus insertions only, else None."""
    sm = difflib.SequenceMatcher(None, old, new, autojunk=False)
    chunks = []
    for op, a1, a2, b1, b2 in sm.get_opcodes():
        if op == 'equal':
            continue
        if op != 'insert':
            return None
        chunks.append(new[b1:b2])
    return chunks


def run_verify(args):
    changed = subprocess.run(['git', 'diff', '--name-only', args.base], cwd=REPO, check=True,
                             stdout=subprocess.PIPE).stdout.decode().split()
    problems, n_lines, n_inline = [], 0, 0
    for rel in changed:
        if rel.startswith('tools/'):
            continue
        old = git_show(args.base, rel)
        if old is None:
            problems.append('%s: new file' % rel)
            continue
        with open(REPO / rel, encoding='utf-8', newline='') as fh:
            new = fh.read()
        ol, nl = old.splitlines(keepends=True), new.splitlines(keepends=True)
        if ol and not ol[-1].endswith('\n') and len(nl) >= len(ol) and nl[len(ol) - 1] == ol[-1] + '\n':
            pass
        sm = difflib.SequenceMatcher(None, ol, nl, autojunk=False)
        for op, a1, a2, b1, b2 in sm.get_opcodes():
            if op == 'equal':
                continue
            olds, news = ol[a1:a2], nl[b1:b2]
            # pair each original line with the new line it became; extra new lines are twins
            k = 0
            for o in olds:
                while k < len(news) and insert_only(o.rstrip('\n'), news[k].rstrip('\n')) is None:
                    if not twin_ok(news[k], rel):
                        problems.append('%s: line not insert-only near %r' % (rel, o.strip()[:60]))
                        break
                    n_lines += 1
                    k += 1
                if k >= len(news):
                    problems.append('%s: original line lost: %r' % (rel, o.strip()[:60]))
                    break
                chunks = insert_only(o.rstrip('\n'), news[k].rstrip('\n'))
                if chunks:
                    n_inline += 1
                    # difflib may split or shift an insertion along repeated text ("布局2 / Layout 2"),
                    # so check what was added to the line as a whole: CJK-free and carrying the separator
                    added = ''.join(chunks)
                    if CJK.search(added) or '/' not in added:
                        problems.append('%s: odd inline insert %r in %r' % (rel, added, news[k].strip()[:80]))
                k += 1
            for extra in news[k:]:
                if not twin_ok(extra, rel):
                    problems.append('%s: inserted line is not an English comment: %r' % (rel, extra.strip()[:80]))
                n_lines += 1
    print(json.dumps({'files': len(changed), 'twin_lines': n_lines, 'inline_inserts': n_inline,
                      'problems': len(problems)}, indent=1))
    for p in problems[:50]:
        print('PROBLEM', p)
    return 1 if problems else 0


def twin_ok(line, rel):
    t = line.strip()
    if CJK.search(t):
        return False
    lang = lang_of(rel)
    return bool(twin_marker(t, lang)) or (lang == 'xml' and t.startswith('<!--'))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    ap.add_argument('--only', default=None)
    ap.add_argument('--report', default=None)
    ap.add_argument('--tm', default=None)
    ap.add_argument('--overrides', default=None)
    ap.add_argument('--verify', action='store_true')
    ap.add_argument('--base', default='HEAD')
    args = ap.parse_args()
    sys.exit(run_verify(args) if args.verify else run_apply(args))


if __name__ == '__main__':
    main()
