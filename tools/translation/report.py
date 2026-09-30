#!/usr/bin/env python3
"""List every CJK line still present in scope (minus the keep-list). Exit 1 if any.

--glossary-lint additionally greps files changed vs. main for banned wording and checks the
WalkRemind placeholder invariant.
"""
import argparse
import re
import subprocess
import sys
from collections import defaultdict

from common import CJK, REPO, load_keep_list, read_lines, scope_files

BANNED = [
    (re.compile(r'\bbracelet\b', re.I), 'use "band"'),
    (re.compile(r'\bquick config', re.I), 'use "fast config"'),
    (re.compile(r'\bsucess', re.I), 'typo: success'),
    (re.compile(r'\b(is|are|was) not support\b', re.I), 'use "not supported"'),
]
WALK_KT = 'app/src/main/java/com/example/example_android/activity/WalkRemindActivity.kt'
WALK_XML = 'app/src/main/res/layout/activity_walk_remind.xml'


def residual():
    keep = load_keep_list()
    found = defaultdict(list)
    for rel in scope_files():
        for i, line in enumerate(read_lines(rel)):
            if CJK.search(line) and line.strip() not in keep:
                found[rel].append((i + 1, line.rstrip('\n')))
    total = sum(len(v) for v in found.values())
    for rel, items in sorted(found.items()):
        print('%s (%d)' % (rel, len(items)))
        for n, l in items[:8]:
            print('   %5d: %s' % (n, l.strip()[:120]))
        if len(items) > 8:
            print('   ...')
    print('residual CJK lines: %d in %d files' % (total, len(found)))
    return total


def glossary_lint():
    changed = subprocess.run(['git', 'diff', '--name-only', 'main', '--'], cwd=REPO, check=True,
                             stdout=subprocess.PIPE).stdout.decode().split()
    problems = 0
    for rel in changed:
        if not (REPO / rel).is_file() or rel.startswith('tools/'):
            continue
        # only lines added/changed relative to main
        diff = subprocess.run(['git', 'diff', '-U0', 'main', '--', rel], cwd=REPO,
                              stdout=subprocess.PIPE).stdout.decode('utf-8', 'replace')
        lineno = 0
        for dl in diff.splitlines():
            m = re.match(r'^@@ -\d+(?:,\d+)? \+(\d+)', dl)
            if m:
                lineno = int(m.group(1))
                continue
            if dl.startswith('+++') or not dl.startswith('+'):
                continue
            for rx, hint in BANNED:
                if rx.search(dl):
                    print('LINT %s:%d %s | %s' % (rel, lineno, hint, dl[1:].strip()[:100]))
                    problems += 1
            lineno += 1
    for proj in ('example_android', 'example_android_lite'):
        kt = ''.join(read_lines('%s/%s' % (proj, WALK_KT)))
        xml = ''.join(read_lines('%s/%s' % (proj, WALK_XML)))
        m_start = re.search(r'tv_log\.text\.toString\(\)\.startsWith\("([^"]+)"\)', kt)
        m_set = re.search(r'tv_log\.text = "([^"]+)"', kt)
        m_xml = re.search(r'android:text="([^"]+)"[^>]*\n[^>]*textColor="#8CF08C"', xml) or \
            re.search(r'android:id="@\+id/tv_log"[\s\S]*?android:text="([^"]+)"', xml)
        ok = m_start and m_set and m_xml and m_set.group(1).startswith(m_start.group(1)) \
            and m_xml.group(1).startswith(m_start.group(1))
        print('WalkRemind invariant %s: %s' % (proj, 'OK' if ok else 'BROKEN'))
        if not ok:
            problems += 1
    return problems


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--glossary-lint', action='store_true')
    args = ap.parse_args()
    total = residual()
    problems = glossary_lint() if args.glossary_lint else 0
    sys.exit(1 if (total or problems) else 0)


if __name__ == '__main__':
    main()
