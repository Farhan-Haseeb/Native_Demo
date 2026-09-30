#!/usr/bin/env python3
"""Merge filled batches into tm.json.

Usage: merge.py BATCH_DIR FILLED_DIR
For every BATCH_DIR/<name>.json with a FILLED_DIR/<name>.json, adds {text: en}. Fails on conflicting
duplicates (same text, different English) and reports entries that are missing from the filled files.
"""
import json
import sys
from pathlib import Path

from common import load_tm, save_tm


def main():
    bdir, fdir = Path(sys.argv[1]), Path(sys.argv[2])
    tm = load_tm()
    added = missing = conflicts = 0
    for bfile in sorted(bdir.glob('*-*.json')):
        ffile = fdir / bfile.name
        if not ffile.exists():
            print('no filled file for', bfile.name)
            continue
        batch = json.load(open(bfile, encoding='utf-8'))
        filled = {str(k): v for k, v in json.load(open(ffile, encoding='utf-8')).items()}
        for e in batch['entries']:
            en = filled.get(str(e['id']))
            force = False
            if isinstance(en, dict):
                force, en = bool(en.get('force')), en.get('en')
            if not isinstance(en, str) or not en.strip():
                missing += 1
                continue
            en = en.strip()
            old = tm.get(e['text'])
            if old is not None and (old if isinstance(old, str) else old.get('en')) != en:
                print('CONFLICT %s\n   old: %s\n   new: %s' % (e['text'][:80], old, en))
                conflicts += 1
                continue
            tm[e['text']] = {'en': en, 'force': True} if force else en
            added += 1
    save_tm(tm)
    print('added=%d missing=%d conflicts=%d total_tm=%d' % (added, missing, conflicts, len(tm)))
    sys.exit(1 if conflicts else 0)


if __name__ == '__main__':
    main()
