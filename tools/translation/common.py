"""Shared helpers for the Chinese -> English translation pipeline.

Scope rules, CJK detection and file iteration live here so that
extract.py, apply.py, report.py and strip_bilingual_blocks.py agree.
"""
import fnmatch
import json
import os
import re
import subprocess
import sys
from pathlib import Path

TOOLS_DIR = Path(__file__).resolve().parent
REPO = TOOLS_DIR.parents[1]

# Han ideographs + CJK punctuation + full-width forms.
# Deliberately excludes x (U+00D7), degree sign, em dash and ellipsis which appear in English lines.
CJK = re.compile(r'[　-〿㐀-䶿一-鿿豈-﫿＀-￯]')

INCLUDE_DIRS = [
    'example_android',
    'example_android_lite',
    'example_ios/IDODemo-Cocoapods',
    'example_ios/IDODemo-Manual',
    'example_ohos/entry/src/main/ets',
]
INCLUDE_FILES = ['CHANGELOG.md', 'example_android_lite/readme']
EXTS = {'.kt', '.java', '.swift', '.m', '.h', '.ets', '.ts', '.xml', '.gradle', '.pro',
        '.plist', '.pbxproj', '.md', '.json'}
EXCLUDE_GLOBS = [
    'CHANGELOG_zh.md',
    'example_ios/README_zh.md',
    '*/res/values-zh*/*',
    '*/resources/zh_CN/*',
    '*/Localizable.xcstrings',
    '*/Common/L10n.swift',
    '*/files_trans/contact/a.json',
    '*/plugins/GeneratedPluginRegistrant.ets',
    '*/flutter_assets/*',
    '*/scripts/sync_strings.py',
    '*/scripts/README.md',
    '*.docx',
    '*/Pods/*',
    '*/Podfile.lock',
]

LANG_BY_EXT = {'.kt': 'kt', '.java': 'java', '.swift': 'swift', '.m': 'objc', '.h': 'objc',
               '.ets': 'ets', '.ts': 'ets', '.xml': 'xml', '.gradle': 'gradle', '.pro': 'pro',
               '.plist': 'plist', '.pbxproj': 'pbxproj', '.md': 'md', '.json': 'json'}


def lang_of(rel_path):
    return LANG_BY_EXT.get(Path(rel_path).suffix, 'txt')


def tracked_files():
    out = subprocess.run(['git', 'ls-files', '-z'], cwd=REPO, check=True,
                         stdout=subprocess.PIPE).stdout.decode('utf-8')
    return [p for p in out.split('\0') if p]


def is_excluded(rel):
    return any(fnmatch.fnmatch(rel, g) for g in EXCLUDE_GLOBS)


def is_binary(path):
    with open(path, 'rb') as fh:
        return b'\0' in fh.read(8192)


def scope_files():
    """Tracked, in-scope, text files (repo-relative posix paths, sorted)."""
    files = []
    for rel in tracked_files():
        if rel in INCLUDE_FILES:
            pass
        elif any(rel.startswith(d + '/') for d in INCLUDE_DIRS) and Path(rel).suffix in EXTS:
            pass
        else:
            continue
        if is_excluded(rel):
            continue
        if not (REPO / rel).is_file():  # sparse / partial checkouts
            continue
        if is_binary(REPO / rel):
            continue
        files.append(rel)
    return sorted(files)


def load_keep_list():
    p = TOOLS_DIR / 'keep_list.txt'
    if not p.exists():
        return set()
    return {ln.strip() for ln in p.read_text(encoding='utf-8').splitlines()
            if ln.strip() and not ln.startswith('#')}


def load_tm(path=None):
    """tm.json: {zh_stripped: en} or {zh_stripped: {"en": ..., "force": bool, "note": ...}}."""
    p = Path(path) if path else TOOLS_DIR / 'tm.json'
    if not p.exists():
        return {}
    return json.loads(p.read_text(encoding='utf-8'))


def tm_entry(value):
    """Normalise a TM value to (en, force)."""
    if isinstance(value, str):
        return value, False
    return value.get('en', ''), bool(value.get('force', False))


def save_tm(tm, path=None):
    p = Path(path) if path else TOOLS_DIR / 'tm.json'
    p.write_text(json.dumps(tm, ensure_ascii=False, indent=1, sort_keys=True) + '\n', encoding='utf-8')


def read_lines(rel):
    return (REPO / rel).read_text(encoding='utf-8').splitlines(keepends=True)


def split_ws(line):
    """Return (leading_ws, stripped, trailing_ws_incl_newline)."""
    stripped = line.strip()
    if not stripped:
        return line, '', ''
    start = line.index(stripped[0]) if stripped else 0
    # find start precisely: first non-space char
    i = 0
    while i < len(line) and line[i] in ' \t':
        i += 1
    j = len(line)
    while j > i and line[j - 1] in ' \t\r\n':
        j -= 1
    return line[:i], line[i:j], line[j:]
