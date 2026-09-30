#!/usr/bin/env python3
"""Collect every CJK-containing line in scope that has no translation yet, dedupe on the
whitespace-stripped line, and write translator batches.

Usage: extract.py --out DIR [--batch-size 150]
Writes DIR/<cluster>-<nn>.json and DIR/manifest.json.
"""
import argparse
import json
import re
from collections import OrderedDict, defaultdict
from pathlib import Path

from common import CJK, lang_of, load_keep_list, load_tm, read_lines, scope_files

PLACEHOLDER = re.compile(r'''\$\{[^}"'`]*\}|\$[A-Za-z_]\w*|\\\([^)"']*\)|%\d?\$?[@dsfl]|\{\}|\\n|\\t''')
CODE_GAP = re.compile(r'[A-Za-z0-9_$.(){}\[\]"\'`]{4,}')

# Cluster assignment by first-occurrence file (keeps a file family with one translator).
def cluster_of(rel):
    name = Path(rel).name
    if rel.startswith('example_android'):
        if name in ('SetFunctionDetailActivity.kt', 'CustomEvtType.kt', 'SdkFeatureTestActivity.kt'):
            return 'android-core'
        return 'android-rest'
    if rel.startswith('example_ios'):
        if name in ('SdkFeatureTestVC.swift', 'SportListVC.swift'):
            return 'ios-core'
        if name in ('SetFunctionVC.swift', 'GetFunctionVC.swift', 'TransferFileVC.swift',
                    'WatchFaceVC.swift', 'SportVC.swift'):
            return 'ios-pages'
        return 'ios-rest'
    if rel.startswith('example_ohos'):
        return 'ohos'
    return 'docs'


def flags_for(text, lang):
    flags = []
    if '\t' in text:
        flags.append('tab')
    if PLACEHOLDER.search(text):
        flags.append('placeholder')
    if re.search(r'[一-鿿][^"\'`]*\s/\s*[A-Za-z]', text) or re.search(r'[A-Za-z]\s*/\s*[一-鿿]', text):
        flags.append('bilingual')
    runs = list(CJK.finditer(text))
    # gaps between consecutive CJK characters that contain code-like tokens
    gaps = [text[a.end():b.start()] for a, b in zip(runs, runs[1:])]
    if any(CODE_GAP.search(g) for g in gaps):
        flags.append('multirun')
    if lang == 'xml' and re.search(r'(android:(text|hint|title|label|contentDescription|summary)|tools:text)\s*=', text):
        flags.append('xml_attr')
    first = runs[0].start()
    head = text[:first]
    if re.match(r'^\s*(///?|/\*+|\*|#|<!--|--)', text) or (head.count('"') % 2 == 0 and '//' in head):
        flags.append('comment')
    elif head.count('"') % 2 == 1 or head.count("'") % 2 == 1 or head.count('`') % 2 == 1:
        flags.append('string')
    return flags


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--out', required=True)
    ap.add_argument('--batch-size', type=int, default=150)
    args = ap.parse_args()
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)

    keep = load_keep_list()
    tm = load_tm()
    entries = OrderedDict()
    covered = 0
    for rel in scope_files():
        lines = read_lines(rel)
        lang = lang_of(rel)
        for i, line in enumerate(lines):
            if not CJK.search(line):
                continue
            text = line.strip()
            if text in keep or text in tm:
                covered += 1
                continue
            loc = '%s:%d' % (rel, i + 1)
            e = entries.get(text)
            if e is None:
                e = entries[text] = {
                    'id': None, 'text': text, 'lang': lang, 'n_occurrences': 0, 'files': [],
                    'ctx': {'before': [l.rstrip('\n') for l in lines[max(0, i - 2):i]],
                            'after': [l.rstrip('\n') for l in lines[i + 1:i + 3]]},
                    'flags': flags_for(text, lang), 'cluster': cluster_of(rel), 'en': '',
                }
            e['n_occurrences'] += 1
            if len(e['files']) < 6:
                e['files'].append(loc)

    by_cluster = defaultdict(list)
    for n, e in enumerate(entries.values(), 1):
        e['id'] = n
        by_cluster[e['cluster']].append(e)

    manifest = {'total_unique': len(entries), 'already_covered_lines': covered, 'clusters': {}, 'batches': []}
    for cluster, items in by_cluster.items():
        manifest['clusters'][cluster] = {'unique': len(items),
                                         'occurrences': sum(e['n_occurrences'] for e in items)}
        for b, k in enumerate(range(0, len(items), args.batch_size), 1):
            chunk = items[k:k + args.batch_size]
            name = '%s-%02d.json' % (cluster, b)
            (out / name).write_text(json.dumps({'cluster': cluster, 'entries': chunk},
                                               ensure_ascii=False, indent=1) + '\n', encoding='utf-8')
            manifest['batches'].append({'file': name, 'entries': len(chunk),
                                        'lines': sum(e['n_occurrences'] for e in chunk)})
    (out / 'manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=1) + '\n', encoding='utf-8')
    print(json.dumps({k: v for k, v in manifest.items() if k != 'batches'}, ensure_ascii=False, indent=1))
    for b in manifest['batches']:
        print('  %-24s entries=%4d lines=%4d' % (b['file'], b['entries'], b['lines']))


if __name__ == '__main__':
    main()
