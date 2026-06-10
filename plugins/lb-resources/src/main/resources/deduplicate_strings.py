#!/usr/bin/env python3
"""Remove duplicate <string>/<plurals> resources from an Android strings file.

Loco stores some keys under more than one asset id that differ only by their
separators (e.g. ``alert.past.training.title`` and ``alert_past_training.title``).
``downloadStrings.sh`` normalises every ``.`` in a resource name to ``_``, so those
ids collapse to the same Android name and end up as duplicate entries in the
downloaded file. This drops the later duplicates, keeping the first occurrence
(and its exact formatting). Idempotent: a file with unique names is left untouched.

Usage: deduplicate_strings.py <strings.xml> <values-dir-name>

Works on raw text lines so the rest of the file keeps its exact formatting.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

# Opening tag of a named resource block, e.g. <string name="x"> or <plurals name="y">.
OPEN_RE = re.compile(r'<(string|plurals)\b[^>]*\bname="([^"]+)"')


def deduplicate(lines: list[str]) -> tuple[list[str], int]:
    output: list[str] = []
    seen: set[tuple[str, str]] = set()
    removed = 0
    index = 0
    total = len(lines)

    while index < total:
        line = lines[index]
        match = OPEN_RE.search(line)
        if match is None:
            output.append(line)
            index += 1
            continue

        tag, name = match.group(1), match.group(2)
        close_tag = f"</{tag}>"
        if re.search(r"/>\s*$", line) or close_tag in line:
            end = index
        else:
            end = index + 1
            while end < total and close_tag not in lines[end]:
                end += 1
        block = lines[index: end + 1]

        key = (tag, name)
        if key in seen:
            removed += len(block)
        else:
            seen.add(key)
            output.extend(block)
        index = end + 1

    return output, removed


def main() -> None:
    file_path = Path(sys.argv[1])
    dir_name = sys.argv[2]
    lines = file_path.read_text(encoding="utf-8").splitlines(keepends=True)
    output, removed = deduplicate(lines)
    if removed:
        file_path.write_text("".join(output), encoding="utf-8")
        print(f"Removed {removed} duplicate resource line(s) in {dir_name}")


if __name__ == "__main__":
    main()
