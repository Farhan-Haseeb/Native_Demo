#!/usr/bin/env python3
"""Delete a Chinese line comment when the very next line is an English line comment (a bilingual pair).

Pattern: `/// 获取设备信息` followed by `/// Get device information` (Swift `///` doc comments, same indent).
Usage: strip_bilingual_line_pairs.py [--dry-run] [FILE ...]   (default: all in-scope code files)
"""
import argparse
import re

from common import CJK, REPO, lang_of, scope_files

MARK = re.compile(r'^(\s*)(///)\s*(.*)$')  # Swift doc comments only; plain // pairs are usually commented-out code
PROSE = re.compile(r'[A-Za-z]{2,}[^A-Za-z\n]+[A-Za-z]{2,}')


def process(rel, dry_run):
    lines = (REPO / rel).read_text(encoding='utf-8').splitlines(keepends=True)
    drop = []
    for i in range(len(lines) - 1):
        a, b = MARK.match(lines[i]), MARK.match(lines[i + 1])
        if not a or not b or a.group(2) != b.group(2) or a.group(1) != b.group(1):
            continue
        if not CJK.search(a.group(3)) or CJK.search(b.group(3)):
            continue
        # the Chinese line must be pure prose (no code before the comment marker), the English line real prose
        if not PROSE.search(b.group(3)) or b.group(3).lstrip().startswith('@'):
            continue
        # skip commented-out code on the English side
        if re.match(r'^\s*(add\(|case |if |let |var |return|\.|\w+\()', b.group(3)):
            continue
        drop.append(i)
    if drop and not dry_run:
        keep = [l for i, l in enumerate(lines) if i not in set(drop)]
        (REPO / rel).write_text(''.join(keep), encoding='utf-8')
    return [(i + 1, lines[i].strip(), lines[i + 1].strip()) for i in drop]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    ap.add_argument('--show', type=int, default=3)
    ap.add_argument('files', nargs='*')
    args = ap.parse_args()
    files = args.files or [f for f in scope_files() if lang_of(f) in ('kt', 'java', 'swift', 'objc', 'ets')]
    total = 0
    for rel in files:
        pairs = process(rel, args.dry_run)
        if pairs:
            total += len(pairs)
            print('%s: %d pairs' % (rel, len(pairs)))
            for n, zh, en in pairs[:args.show]:
                print('   %5d  %s  ==>  %s' % (n, zh[:60], en[:60]))
    print('total pairs%s: %d' % (' (dry run)' if args.dry_run else ' deleted', total))


if __name__ == '__main__':
    main()
