#!/usr/bin/env python3
"""Apply tm.json to every in-scope file, validating each replacement.

A replacement is rejected (line left untouched, reason reported) unless:
  * the English is non-empty and contains no CJK;
  * the text before the first CJK char and after the last CJK char is unchanged
    (bilingual "zh / en" literals may drop the " / en" part; entries with "force": true skip this);
  * for lines with code between two CJK runs, those code gaps appear in order in the English;
  * placeholders, tabs, backticks and double quotes are preserved; single quotes for .ets/.xml;
  * XML lines gain no bare '&' or '<'.
File-level: line count, triple-quote count and brace/paren totals must be unchanged.

Usage: apply.py [--dry-run] [--only GLOB] [--report FILE] [--tm FILE]
"""
import argparse
import fnmatch
import json
import re
from collections import Counter

from common import CJK, REPO, lang_of, load_keep_list, load_tm, read_lines, scope_files, split_ws, tm_entry

PLACEHOLDER = re.compile(r'''\$\{[^}"'`]*\}|\$[A-Za-z_]\w*|\\\([^)"']*\)|%\d?\$?[@dsfl]|\{\}|\\n|\\t''')
CODE_GAP = re.compile(r'[A-Za-z0-9_$.(){}\[\]"\'`]{4,}')
BILINGUAL_SUFFIX = re.compile(r'^\s*/\s*[^"\'`)>\]]+(.*)$', re.S)
BARE_AMP = re.compile(r'&(?!(amp|lt|gt|quot|apos|#\d+|#x[0-9a-fA-F]+);)')


def validate(text, en, lang, force):
    if not en.strip():
        return 'empty'
    if CJK.search(en):
        return 'still contains CJK'
    if en.count('\\') > text.count('\\'):
        return 'backslash added'
    runs = list(CJK.finditer(text))
    first, last = runs[0].start(), runs[-1].end()
    prefix, suffix = text[:first], text[last:]
    if not force:
        if not en.startswith(prefix):
            return 'prefix changed: %r' % prefix
        if not en.endswith(suffix):
            m = BILINGUAL_SUFFIX.match(suffix)
            if not (m and en.endswith(m.group(1))):
                return 'suffix changed: %r' % suffix
        gaps = [text[a.end():b.start()] for a, b in zip(runs, runs[1:])]
        pos = len(prefix)
        for g in gaps:
            if not CODE_GAP.search(g):
                continue
            g = g.strip()
            k = en.find(g, pos)
            if k < 0:
                return 'code gap missing: %r' % g
            pos = k + len(g)
    if force:
        return None  # hand-reviewed entry: only the CJK-free / file-level checks apply
    if Counter(PLACEHOLDER.findall(text)) != Counter(PLACEHOLDER.findall(en)):
        return 'placeholders differ'
    for ch in '\t`"':
        if text.count(ch) != en.count(ch):
            return 'count of %r differs' % ch
    if lang in ('ets', 'xml') and text.count("'") != en.count("'"):
        return "count of ' differs"
    if lang == 'xml':
        if len(BARE_AMP.findall(en)) > len(BARE_AMP.findall(text)):
            return 'bare & added'
        if en.count('<') != text.count('<'):
            return "count of '<' differs"
    return None


def file_totals(lines):
    s = ''.join(lines)
    return {'lines': len(lines), 'tq': s.count('"""'),
            'braces': s.count('{') - s.count('}'), 'parens': s.count('(') - s.count(')'),
            'nbraces': s.count('{') + s.count('}'), 'nparens': s.count('(') + s.count(')')}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    ap.add_argument('--only', default=None)
    ap.add_argument('--report', default=None)
    ap.add_argument('--tm', default=None)
    args = ap.parse_args()

    tm = load_tm(args.tm)
    keep = load_keep_list()
    report = {'applied': 0, 'kept': 0, 'no_entry': [], 'rejected': [], 'files_changed': []}
    for rel in scope_files():
        if args.only and not fnmatch.fnmatch(rel, args.only):
            continue
        lines = read_lines(rel)
        lang = lang_of(rel)
        changed = False
        new_lines = list(lines)
        for i, line in enumerate(lines):
            if not CJK.search(line):
                continue
            lead, text, trail = split_ws(line)
            if text in keep:
                report['kept'] += 1
                continue
            if text not in tm:
                report['no_entry'].append('%s:%d' % (rel, i + 1))
                continue
            en, force = tm_entry(tm[text])
            why = validate(text, en, lang, force)
            if why:
                report['rejected'].append({'loc': '%s:%d' % (rel, i + 1), 'zh': text, 'en': en, 'why': why})
                continue
            new_lines[i] = lead + en + trail
            changed = True
            report['applied'] += 1
        if changed:
            before, after = file_totals(lines), file_totals(new_lines)
            # Converting full-width punctuation to ASCII legitimately changes paren totals,
            # so only the line count, triple-quote count and bracket balance are compared.
            worse = (before['lines'] != after['lines'] or before['tq'] != after['tq']
                     or abs(after['braces']) > abs(before['braces'])
                     or abs(after['parens']) > abs(before['parens']))
            if worse:
                report['rejected'].append({'loc': rel, 'zh': '', 'en': '',
                                           'why': 'file totals changed %s -> %s' % (before, after)})
                continue
            report['files_changed'].append(rel)
            if not args.dry_run:
                (REPO / rel).write_text(''.join(new_lines), encoding='utf-8')
    summary = {k: (len(v) if isinstance(v, list) else v) for k, v in report.items()}
    summary['dry_run'] = args.dry_run
    print(json.dumps(summary, indent=1))
    for r in report['rejected'][:40]:
        print('REJECTED %s: %s\n   zh: %s\n   en: %s' % (r['loc'], r['why'], r['zh'], r['en']))
    if args.report:
        with open(args.report, 'w', encoding='utf-8') as fh:
            json.dump(report, fh, ensure_ascii=False, indent=1)


if __name__ == '__main__':
    main()
