#!/usr/bin/env python3
"""Delete the Chinese half of bilingual block comments.

Many KDoc blocks in the demos are written as "Chinese table, blank line, English table".
For every /* ... */ block that contains CJK text and is followed (inside the same block) by
an English section at least RATIO as long, this removes the CJK section (first CJK line through
last CJK line, plus one blank ` *` separator that follows). Blocks that fail the ratio are left alone.

Usage: strip_bilingual_blocks.py [--dry-run] [--ratio 0.7] [--min-lines 4] [FILE ...]
Default files: the two SetFunctionDetailActivity.kt files.
"""
import argparse
import re
import sys

from common import CJK, REPO

DEFAULT_FILES = [
    'example_android/app/src/main/java/com/example/example_android/activity/SetFunctionDetailActivity.kt',
    'example_android_lite/app/src/main/java/com/example/example_android/activity/SetFunctionDetailActivity.kt',
]
BLOCK = re.compile(r'/\*[\s\S]*?\*/')
PROSE = re.compile(r'[A-Za-z]{2,}[^A-Za-z\n]+[A-Za-z]{2,}')  # at least two English words
TAG = re.compile(r'^\s*\*?\s*@')  # @param / @return lines are not prose


def blocks_of(text):
    """Yield (start_line, end_line) inclusive line indexes for each block comment."""
    line_starts = [0]
    for m in re.finditer('\n', text):
        line_starts.append(m.end())

    def line_at(pos):
        lo, hi = 0, len(line_starts) - 1
        while lo < hi:
            mid = (lo + hi + 1) // 2
            if line_starts[mid] <= pos:
                lo = mid
            else:
                hi = mid - 1
        return lo

    for m in BLOCK.finditer(text):
        yield line_at(m.start()), line_at(m.end() - 1)


def process(rel, ratio, min_lines, dry_run):
    path = REPO / rel
    text = path.read_text(encoding='utf-8')
    lines = text.splitlines(keepends=True)
    to_delete = set()
    stats = {'blocks': 0, 'stripped': 0, 'refused': 0, 'deleted_lines': 0}
    edits = []  # (line_idx, new_text) for opener/closer lines that also carry CJK
    for start, end in blocks_of(text):
        block = lines[start:end + 1]
        cjk = [i for i, l in enumerate(block) if CJK.search(l)]
        if not cjk:
            continue
        stats['blocks'] += 1
        first, last = min(cjk), max(cjk)
        tail = block[last + 1:]
        # ignore a bare closing "*/" line when counting prose
        prose = sum(1 for l in tail if PROSE.search(l.replace('*/', '')) and not TAG.match(l))
        if len(cjk) < min_lines or prose < ratio * len(cjk):
            stats['refused'] += 1
            print('  refused %s:%d (cjk=%d, english=%d)' % (rel, start + first + 1, len(cjk), prose))
            continue
        # Lines to delete: first..last inclusive, plus a following blank " *" separator.
        del_range = list(range(start + first, start + last + 1))
        nxt = start + last + 1
        if nxt <= end and lines[nxt].strip() in ('*', ''):
            del_range.append(nxt)
        # If the first CJK line is also the block opener, keep just the opener.
        opener = block[first]
        if opener.lstrip().startswith('/*'):
            indent = opener[:len(opener) - len(opener.lstrip())]
            tok = '/**' if opener.lstrip().startswith('/**') else '/*'
            edits.append((start + first, indent + tok + '\n'))
            del_range.remove(start + first)
        closer = block[last]
        if closer.rstrip().endswith('*/') and (start + last) in del_range:
            indent = closer[:len(closer) - len(closer.lstrip())]
            edits.append((start + last, indent + '*/\n'))
            del_range.remove(start + last)
        to_delete.update(del_range)
        stats['stripped'] += 1
        stats['deleted_lines'] += len(del_range)
    if not dry_run and (to_delete or edits):
        for idx, new in edits:
            lines[idx] = new
        lines = [l for i, l in enumerate(lines) if i not in to_delete]
        path.write_text(''.join(lines), encoding='utf-8')
    return stats


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    ap.add_argument('--ratio', type=float, default=0.7)
    ap.add_argument('--min-lines', type=int, default=4)
    ap.add_argument('files', nargs='*')
    args = ap.parse_args()
    files = args.files or DEFAULT_FILES
    total = 0
    for rel in files:
        s = process(rel, args.ratio, args.min_lines, args.dry_run)
        print('%s: blocks=%d stripped=%d refused=%d deleted_lines=%d%s' % (
            rel, s['blocks'], s['stripped'], s['refused'], s['deleted_lines'],
            ' (dry run)' if args.dry_run else ''))
        total += s['deleted_lines']
    print('total deleted lines:', total)


if __name__ == '__main__':
    main()
