# Translation tooling

Scripts used to translate the Chinese comments, UI strings and log messages of the demo projects
to English, and to re-apply that translation after merging a new upstream release.

| File | Purpose |
|---|---|
| `common.py` | Scope rules (which files are in scope, which are excluded), CJK detection, helpers |
| `keep_list.txt` | Exact source lines that intentionally keep Chinese (e.g. the `"中文"` language label) |
| `glossary.md` | Mandatory terminology (Chinese -> English), taken from the repo's own English resources |
| `strip_bilingual_blocks.py` | Deletes the Chinese half of block comments that already contain an English copy |
| `strip_bilingual_line_pairs.py` | Deletes a `/// Chinese` doc line when the next line is its `/// English` twin |
| `extract.py` | Collects every CJK line without a translation, dedupes it, writes translator batches |
| `validate_batch.py` | Checks a filled batch against the rules before merging |
| `merge.py` | Merges filled batches into `tm.json` (the translation memory) |
| `apply.py` | Rewrites every in-scope line that has an entry in `tm.json`, with structural validation |
| `report.py` | Lists residual CJK lines; `--glossary-lint` also checks wording and the WalkRemind invariant |
| `apply_review.py` | Applies reviewer suggestions (`{file, line, current, suggested}`) to the files and to `tm.json` |

## Re-syncing with upstream

```bash
git checkout main && git pull upstream main
git checkout english && git merge main
python3 tools/translation/strip_bilingual_blocks.py            # Chinese halves of bilingual KDoc blocks
python3 tools/translation/strip_bilingual_line_pairs.py        # /// Chinese lines that have an English twin
python3 tools/translation/extract.py --out /tmp/batches        # only lines that are new since last time
# translate the batches (see the rules in validate_batch.py / the agent contract), write /tmp/filled/<batch>.json
python3 tools/translation/validate_batch.py /tmp/batches/X.json /tmp/filled/X.json
python3 tools/translation/merge.py /tmp/batches /tmp/filled
python3 tools/translation/apply.py --report /tmp/apply_report.json
python3 tools/translation/report.py --glossary-lint            # must print 0 residual lines
```

## What apply.py validates

For every replaced line: the code before the first and after the last Chinese character is unchanged
(bilingual `zh / en` literals may drop the Chinese half), code between two Chinese runs is preserved in
order, placeholders (`${x}`, `$x`, `\(x)`, `%@`, `\n`, ...), tabs, backticks and double quotes are preserved
(single quotes too for `.ets`/`.xml`), and XML gains no bare `&`/`<`. Per file: line count, `"""` count
and brace/paren totals are unchanged. Entries in `tm.json` may be `{"en": ..., "force": true}` to bypass
the prefix/suffix rule for the few lines that legitimately reorder text.

## Not translated on purpose

Chinese locale resources (`res/values-zh/`, `resources/zh_CN/`, `zh-Hans` in `Localizable.xcstrings`),
`CHANGELOG_zh.md`, `example_ios/README_zh.md`, the Chinese `.docx`, the test contact list
`files_trans/contact/a.json`, generated files (`L10n.swift`, `GeneratedPluginRegistrant.ets`), SDK
binaries (`.aar`, `.har`, `flutter_assets/`), and the language-code-1 `"中文"` entries.
