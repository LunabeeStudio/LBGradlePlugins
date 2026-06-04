#!/usr/bin/env python3
"""Duplicate missing plural forms in an Android strings file.

Some locales need plural forms that translators rarely fill on the provider
(e.g. Android requires `many` for French). For each plurals block, the value of
a source form is copied to the listed target forms — but only when the target
form is not already present, so files with complete plurals are left untouched
and the operation is idempotent.

Usage: duplicate_plural_forms.py <strings.xml> <values-dir-name>

Works on raw text lines so the rest of the file keeps its exact formatting.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

# values dir suffix -> list of (source form, [target forms]) rules.
RULES: dict[str, list[tuple[str, list[str]]]] = {
    "values-ar": [("two", ["few", "many", "other"])],
    "values-fr": [("other", ["many"])],
    "values-es": [("other", ["many"])],
    "values-pt": [("other", ["many"])],
    "values-it": [("other", ["many"])],
    "values-pl": [("few", ["many"])],
    "values-ru": [("few", ["many"])],
    "values-uk": [("few", ["many"])],
}

ITEM_RE = re.compile(r'(\s*<item quantity=")([a-z]+)(".*)')


def process(lines: list[str], rules: list[tuple[str, list[str]]]) -> tuple[list[str], int]:
    output: list[str] = []
    block: list[str] | None = None
    added = 0

    def flush_block(block_lines: list[str]) -> list[str]:
        nonlocal added
        forms: dict[str, str] = {}
        for line in block_lines:
            match = ITEM_RE.match(line)
            if match:
                forms[match.group(2)] = line
        result = list(block_lines)
        for source, targets in rules:
            source_line = forms.get(source)
            if source_line is None:
                continue
            missing = [t for t in targets if t not in forms]
            if not missing:
                continue
            insert_at = result.index(source_line) + 1
            for target in missing:
                duplicated = ITEM_RE.sub(rf"\g<1>{target}\g<3>", source_line)
                result.insert(insert_at, duplicated)
                insert_at += 1
                forms[target] = duplicated
                added += 1
        return result

    for line in lines:
        if block is not None:
            block.append(line)
            if "</plurals>" in line:
                output.extend(flush_block(block))
                block = None
            continue
        if "<plurals" in line and "</plurals>" not in line:
            block = [line]
            continue
        output.append(line)

    if block is not None:
        # Unterminated block: keep as-is rather than guessing.
        output.extend(block)

    return output, added


def main() -> None:
    file_path = Path(sys.argv[1])
    dir_name = sys.argv[2]
    rules = RULES.get(dir_name)
    if rules is None:
        return
    lines = file_path.read_text(encoding="utf-8").splitlines(keepends=True)
    output, added = process(lines, rules)
    if added:
        file_path.write_text("".join(output), encoding="utf-8")
        print(f"Duplicated {added} missing plural form(s) in {dir_name}")


if __name__ == "__main__":
    main()
