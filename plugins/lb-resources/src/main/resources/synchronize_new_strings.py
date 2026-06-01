#!/usr/bin/env python3

from __future__ import annotations

import argparse
import base64
import copy
import json
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path


TOOLS_NAMESPACE = "http://schemas.android.com/tools"
XLIFF_NAMESPACE = "urn:oasis:names:tc:xliff:document:1.2"
LOCO_IMPORT_URL = "https://localise.biz/api/import/xml?index=id&locale=en&format=android"

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
) -> tuple[list[ET.Element], list[tuple[str, str, str]], list[str]]:
    """Return (elements to upload, conflicts, unverified keys).

    - New local keys (present locally, absent remotely) are always uploaded.
    - A key whose value differs between local and remote is a modification:
      it is uploaded only when the dev changed it locally (local != base) and
      the remote value is still the baseline (remote == base). When both sides
      changed it is reported as a conflict and left untouched.
    """
    local_map = index_resources(local_root)
    remote_map = index_resources(remote_root)
    base_map = index_resources(base_root) if base_root is not None else None

    upload: list[ET.Element] = []
    conflicts: list[tuple[str, str, str]] = []
    unverified: list[str] = []

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
            # No baseline entry for this key: cannot verify, skip safely.
            unverified.append(dotted_name(key[1]))
            continue

        local_changed = not values_equal(local_el, base_el)
        remote_changed = not values_equal(remote_el, base_el)

        if not local_changed:
            # Only remote changed; the fresh download already applied it.
            continue
        if remote_changed:
            # Both sides changed since the baseline -> conflict, do not touch.
            conflicts.append((dotted_name(key[1]), display_value(local_el), display_value(remote_el)))
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


def upload_resources(file_path: Path, api_key: str) -> None:
    credentials = f"{api_key}:".encode("utf-8")
    request = urllib.request.Request(
        LOCO_IMPORT_URL,
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


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Diff Android strings.xml files and upload new/modified local entries to Loco.",
    )
    parser.add_argument("--api-key", required=True, help="Loco API key.")
    parser.add_argument(
        "--before",
        required=True,
        type=Path,
        help="Strings file snapshot taken before the latest downloadStrings (local state).",
    )
    parser.add_argument(
        "--after",
        required=True,
        type=Path,
        help="Strings file as it stands after downloadStrings (remote state).",
    )
    parser.add_argument(
        "--base",
        type=Path,
        help=(
            "Baseline strings file (e.g. git HEAD) representing the last synced state. "
            "Required to enable modification sync and conflict detection."
        ),
    )
    parser.add_argument(
        "--output",
        required=True,
        type=Path,
        help="Path where the XML payload uploaded to Loco is written.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the XML payload instead of uploading it.",
    )
    args = parser.parse_args()

    local_root = ET.parse(args.before).getroot()
    remote_root = ET.parse(args.after).getroot()
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

    for name in unverified:
        print(
            f"⚠️  '{name}' differs between local and remote but has no baseline entry; left untouched.",
            file=sys.stderr,
        )
    for name, local_value, remote_value in conflicts:
        print(
            f"❌ Conflict on '{name}': modified both locally and remotely; left untouched.\n"
            f"   local:  {local_value}\n"
            f"   remote: {remote_value}",
            file=sys.stderr,
        )

    if not upload:
        print("No new or modified local resources to upload.")
        return

    if args.dry_run:
        print(output_payload.decode("utf-8"))
        return

    try:
        upload_resources(args.output, args.api_key)
    except RuntimeError as error:
        print(f"❌ {error}", file=sys.stderr)
        sys.exit(1)
    print(f"Uploaded {len(upload)} new/modified local resource(s) to Loco.")


if __name__ == "__main__":
    main()
