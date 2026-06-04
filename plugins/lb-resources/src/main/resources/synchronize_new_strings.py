#!/usr/bin/env python3

from __future__ import annotations

import argparse
import base64
import copy
import json
import re
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path


TOOLS_NAMESPACE = "http://schemas.android.com/tools"
XLIFF_NAMESPACE = "urn:oasis:names:tc:xliff:document:1.2"
LOCO_IMPORT_URL_TEMPLATE = "https://localise.biz/api/import/xml?index=id&locale={locale}&format=android"

ET.register_namespace("tools", TOOLS_NAMESPACE)
ET.register_namespace("xliff", XLIFF_NAMESPACE)


def resource_key(element: ET.Element) -> tuple[str, str] | None:
    name = element.attrib.get("name")
    if not name:
        return None
    return element.tag, name


def index_resources(root: ET.Element) -> dict[tuple[str, str], ET.Element]:
    mapping: dict[tuple[str, str], ET.Element] = {}
    for child in root:
        key = resource_key(child)
        if key is not None:
            mapping[key] = child
    return mapping


def normalized_value(element: ET.Element) -> str:
    clone = copy.deepcopy(element)
    clone.tail = None
    return ET.tostring(clone, encoding="unicode")


def values_equal(a: ET.Element, b: ET.Element) -> bool:
    return normalized_value(a) == normalized_value(b)


def display_value(element: ET.Element) -> str:
    if len(list(element)) == 0:
        return (element.text or "").strip()
    return ET.tostring(element, encoding="unicode").strip()


def dotted_name(name: str) -> str:
    return name.replace("_", ".")


def build_payload(
    local_root: ET.Element,
    remote_root: ET.Element,
    base_root: ET.Element | None,
) -> tuple[list[ET.Element], list[tuple[ET.Element, ET.Element]], list[ET.Element]]:
    """Return (elements to upload, conflicts, unverified elements).

    - New local keys (present locally, absent remotely) are always uploaded.
    - A key whose value differs between local and remote is a modification:
      it is uploaded only when the dev changed it locally (local != base) and
      the remote value is still the baseline (remote == base). When both sides
      changed it is reported as a conflict (local, remote) and left untouched.
    - A differing key with no baseline entry is unverified: Loco is left
      untouched, but the local value is kept on disk (restored after download).
    """
    local_map = index_resources(local_root)
    remote_map = index_resources(remote_root)
    base_map = index_resources(base_root) if base_root is not None else None

    upload: list[ET.Element] = []
    conflicts: list[tuple[ET.Element, ET.Element]] = []
    unverified: list[ET.Element] = []

    for key, local_el in local_map.items():
        remote_el = remote_map.get(key)
        if remote_el is None:
            # New local resource (missing on remote): always push.
            upload.append(local_el)
            continue

        if values_equal(local_el, remote_el):
            continue

        # Local and remote disagree -> potential modification.
        if base_map is None:
            # No baseline available: modification sync disabled, skip safely.
            continue

        base_el = base_map.get(key)
        if base_el is None:
            # No baseline entry for this key: cannot verify, keep the local value on disk.
            unverified.append(local_el)
            continue

        local_changed = not values_equal(local_el, base_el)
        remote_changed = not values_equal(remote_el, base_el)

        if not local_changed:
            # Only remote changed; the fresh download already applied it.
            continue
        if remote_changed:
            # Both sides changed since the baseline -> conflict, do not touch.
            conflicts.append((local_el, remote_el))
            continue

        # Local modified, remote untouched -> safe to push the local value.
        upload.append(local_el)

    return upload, conflicts, unverified


def build_upload_tree(template_root: ET.Element, elements: list[ET.Element]) -> ET.ElementTree:
    output_root = ET.Element(template_root.tag, template_root.attrib)
    output_root.text = "\n    " if elements else "\n"

    uploaded = []
    for element in elements:
        clone = copy.deepcopy(element)
        clone.attrib["name"] = dotted_name(clone.attrib["name"])
        uploaded.append(clone)

    for index, child in enumerate(uploaded):
        child.tail = "\n    " if index < len(uploaded) - 1 else "\n"
        output_root.append(child)

    return ET.ElementTree(output_root)


def serialize_tree(tree: ET.ElementTree) -> bytes:
    return ET.tostring(tree.getroot(), encoding="utf-8", xml_declaration=True)


def extract_loco_error_message(body: str) -> str | None:
    try:
        payload = json.loads(body)
    except json.JSONDecodeError:
        return None
    if isinstance(payload, dict):
        error = payload.get("error")
        if isinstance(error, str) and error:
            return error
    return None


def upload_resources(file_path: Path, api_key: str, locale: str) -> None:
    credentials = f"{api_key}:".encode("utf-8")
    request = urllib.request.Request(
        LOCO_IMPORT_URL_TEMPLATE.format(locale=locale),
        data=file_path.read_bytes(),
        headers={
            "Accept": "application/json",
            "Authorization": f"Basic {base64.b64encode(credentials).decode('ascii')}",
            "Content-Type": "application/xml",
        },
        method="POST",
    )

    try:
        with urllib.request.urlopen(request) as response:
            response_body = response.read().decode("utf-8")
    except urllib.error.HTTPError as error:
        error_body = error.read().decode("utf-8", errors="replace")
        message = extract_loco_error_message(error_body) or error_body.strip()
        raise RuntimeError(f"Loco upload failed (HTTP {error.code}): {message}") from error
    except urllib.error.URLError as error:
        raise RuntimeError(f"Loco upload failed: {error.reason}") from error

    if not response_body:
        return

    payload = json.loads(response_body)
    if payload.get("error"):
        raise RuntimeError(f"Loco upload failed: {payload['error']}")


def find_resource_block(lines: list[str], tag: str, name: str) -> tuple[int, int] | None:
    """Return (start, end) inclusive line indices of the resource block, or None."""
    open_re = re.compile(rf'<{re.escape(tag)}\b[^>]*\bname="{re.escape(name)}"')
    close_tag = f"</{tag}>"
    for index, line in enumerate(lines):
        if not open_re.search(line):
            continue
        if re.search(r"/>\s*$", line) or close_tag in line:
            return index, index
        for end in range(index + 1, len(lines)):
            if close_tag in lines[end]:
                return index, end
        return index, len(lines) - 1
    return None


def run_sync(args: argparse.Namespace) -> None:
    local_root = ET.parse(args.before).getroot()
    # A locale with no translated entry on the provider may be missing from the download:
    # treat it as an empty remote so every local entry is seen as new and uploaded.
    remote_root = (
        ET.parse(args.after).getroot()
        if args.after.exists()
        else ET.Element(local_root.tag)
    )
    base_root = (
        ET.parse(args.base).getroot()
        if args.base is not None and args.base.exists()
        else None
    )

    upload, conflicts, unverified = build_payload(local_root, remote_root, base_root)

    output_tree = build_upload_tree(local_root, upload)
    output_payload = serialize_tree(output_tree)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(output_payload)

    if args.conflicts_output is not None:
        # One "tag\tname" per key whose local value must survive the download (conflicts and
        # unverified modifications), consumed by the restore step.
        restore_elements = [local_el for local_el, _ in conflicts] + unverified
        conflict_lines = "".join(
            f"{el.tag}\t{el.attrib['name']}\n" for el in restore_elements
        )
        args.conflicts_output.parent.mkdir(parents=True, exist_ok=True)
        args.conflicts_output.write_text(conflict_lines, encoding="utf-8")

    for el in unverified:
        print(
            f"⚠️  [{args.locale}] '{dotted_name(el.attrib['name'])}' differs between local and remote but has no "
            f"baseline entry; your local value is kept on disk, Loco is left untouched.",
            file=sys.stderr,
        )
    for local_el, remote_el in conflicts:
        print(
            f"❌ [{args.locale}] Conflict on '{dotted_name(local_el.attrib['name'])}': modified both locally and "
            f"remotely. Your local value is kept on disk; Loco is left untouched.\n"
            f"   local:  {display_value(local_el)}\n"
            f"   remote: {display_value(remote_el)}",
            file=sys.stderr,
        )

    if not upload:
        print(f"[{args.locale}] No new or modified local resources to upload.")
        return

    if args.dry_run:
        print(output_payload.decode("utf-8"))
        return

    try:
        upload_resources(args.output, args.api_key, args.locale)
    except RuntimeError as error:
        print(f"❌ {error}", file=sys.stderr)
        sys.exit(1)
    print(f"Uploaded {len(upload)} new/modified local resource(s) to Loco ({args.locale}).")


def run_restore(args: argparse.Namespace) -> None:
    """Splice the local (source) value of each conflicting key back into the target file.

    Works on raw text lines so the rest of the freshly-downloaded file keeps its exact
    formatting; only the conflicting resource blocks are replaced.
    """
    if not args.conflicts.exists():
        return
    entries = [
        line.split("\t", 1)
        for line in args.conflicts.read_text(encoding="utf-8").splitlines()
        if "\t" in line
    ]
    if not entries:
        return

    source_lines = args.source.read_text(encoding="utf-8").splitlines(keepends=True)
    target_lines = args.target.read_text(encoding="utf-8").splitlines(keepends=True)

    restored = 0
    for tag, name in entries:
        source_block = find_resource_block(source_lines, tag, name)
        target_block = find_resource_block(target_lines, tag, name)
        if source_block is None or target_block is None:
            continue
        replacement = source_lines[source_block[0]: source_block[1] + 1]
        target_lines[target_block[0]: target_block[1] + 1] = replacement
        restored += 1

    if restored:
        args.target.write_text("".join(target_lines), encoding="utf-8")
        print(f"Restored {restored} conflicting local value(s) in {args.target.name}.")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Sync new/modified Android strings with Loco and resolve conflicts locally.",
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    sync = subparsers.add_parser(
        "sync",
        help="Diff strings files and upload new/modified local entries to Loco.",
    )
    sync.add_argument("--api-key", required=True, help="Loco API key.")
    sync.add_argument(
        "--locale",
        default="en",
        help="Loco locale code the diffed files belong to (used for the upload).",
    )
    sync.add_argument(
        "--before",
        required=True,
        type=Path,
        help="Strings file snapshot taken before the latest downloadStrings (local state).",
    )
    sync.add_argument(
        "--after",
        required=True,
        type=Path,
        help="Strings file as it stands after downloadStrings (remote state).",
    )
    sync.add_argument(
        "--base",
        type=Path,
        help=(
            "Baseline strings file (e.g. git HEAD) representing the last synced state. "
            "Required to enable modification sync and conflict detection."
        ),
    )
    sync.add_argument(
        "--output",
        required=True,
        type=Path,
        help="Path where the XML payload uploaded to Loco is written.",
    )
    sync.add_argument(
        "--conflicts-output",
        type=Path,
        dest="conflicts_output",
        help="Path where conflicting keys (tag\\tname per line) are written for the restore step.",
    )
    sync.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the XML payload instead of uploading it.",
    )

    restore = subparsers.add_parser(
        "restore",
        help="Splice local values of conflicting keys back into the downloaded strings file.",
    )
    restore.add_argument("--target", required=True, type=Path, help="Freshly downloaded strings file to patch.")
    restore.add_argument("--source", required=True, type=Path, help="Local snapshot holding the values to keep.")
    restore.add_argument("--conflicts", required=True, type=Path, help="Conflicts file produced by the sync step.")

    args = parser.parse_args()
    if args.command == "sync":
        run_sync(args)
    else:
        run_restore(args)


if __name__ == "__main__":
    main()
