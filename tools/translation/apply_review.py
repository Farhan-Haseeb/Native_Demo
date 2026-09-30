#!/usr/bin/env python3
"""Apply reviewer suggestions: JSON list of {file, line, current, suggested}.

The stripped text at file:line must equal `current`; the line is replaced by `suggested` (same indent).
Every other in-scope line whose stripped text equals `current` is replaced as well (twins in the other
project), and tm.json entries whose English equals `current` are updated so re-applying stays consistent.
"""
import json
import sys

from common import CJK, REPO, load_tm, read_lines, save_tm, scope_files, split_ws


def main():
    items = json.load(open(sys.argv[1], encoding='utf-8'))
    tm = load_tm()
    mapping = {}
    bad = 0
    for it in items:
        cur, sug = it['current'].strip(), it['suggested'].strip()
        if CJK.search(sug) or not sug:
            print('SKIP (bad suggestion):', it['file'], it['line']); bad += 1; continue
        lines = read_lines(it['file'])
        if it['line'] > len(lines) or lines[it['line'] - 1].strip() != cur:
            print('SKIP (line mismatch):', it['file'], it['line'], '|', cur[:60]); bad += 1; continue
        mapping[cur] = sug
    changed = 0
    for rel in scope_files():
        lines = read_lines(rel)
        out, hit = [], False
        for line in lines:
            lead, text, trail = split_ws(line)
            if text in mapping:
                out.append(lead + mapping[text] + trail); hit = True; changed += 1
            else:
                out.append(line)
        if hit:
            (REPO / rel).write_text(''.join(out), encoding='utf-8')
    tm_updates = 0
    for zh, v in tm.items():
        en = v if isinstance(v, str) else v['en']
        if en in mapping:
            tm[zh] = mapping[en] if isinstance(v, str) else dict(v, en=mapping[en]); tm_updates += 1
    save_tm(tm)
    print('suggestions=%d skipped=%d lines_changed=%d tm_updated=%d' % (len(items), bad, changed, tm_updates))


if __name__ == '__main__':
    main()
