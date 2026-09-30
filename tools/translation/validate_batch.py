#!/usr/bin/env python3
"""Validate a translator's filled batch before merging.

Usage: validate_batch.py BATCH.json FILLED.json
FILLED.json is {"<id>": "<english line>", ...} (ids as strings or ints).
Prints one line per problem and a summary; exit 1 if any entry is missing or invalid.
"""
import json
import sys

from apply import validate


def main():
    batch = json.load(open(sys.argv[1], encoding='utf-8'))
    filled = json.load(open(sys.argv[2], encoding='utf-8'))
    filled = {str(k): v for k, v in filled.items() if not str(k).startswith('_')}
    bad = 0
    for e in batch['entries']:
        en = filled.get(str(e['id']))
        if en is None:
            print('MISSING id=%d: %s' % (e['id'], e['text'][:90]))
            bad += 1
            continue
        force = False
        if isinstance(en, dict):
            force, en = bool(en.get('force')), en.get('en')
        if not isinstance(en, str):
            print('NOT A STRING id=%d' % e['id'])
            bad += 1
            continue
        why = validate(e['text'], en.strip(), e['lang'], force)
        if why:
            print('INVALID id=%d (%s)\n   zh: %s\n   en: %s' % (e['id'], why, e['text'], en))
            bad += 1
    print('%d entries, %d problems' % (len(batch['entries']), bad))
    sys.exit(1 if bad else 0)


if __name__ == '__main__':
    main()
