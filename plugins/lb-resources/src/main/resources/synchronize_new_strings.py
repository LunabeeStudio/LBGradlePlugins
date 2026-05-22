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


def build_deleted_resources(before_path: Path, after_path: Path) -> ET.ElementTree:
    before_tree = ET.parse(before_path)
    after_tree = ET.parse(after_path)

    before_root = before_tree.getroot()
    after_root = after_tree.getroot()

    after_keys = {
        key
        for child in after_root
        if (key := resource_key(child)) is not None
    }

    output_root = ET.Element(before_root.tag, before_root.attrib)
    output_root.text = "\n    "

    deleted_resources = []
    for child in before_root:
        key = resource_key(child)
        if key is None or key in after_keys:
            continue

        deleted_child = copy.deepcopy(child)
        deleted_child.attrib["name"] = deleted_child.attrib["name"].replace("_", ".")
        deleted_resources.append(deleted_child)

    for index, child in enumerate(deleted_resources):
        child.tail = "\n    " if index < len(deleted_resources) - 1 else "\n"
        output_root.append(child)

    if not deleted_resources:
        output_root.text = "\n"

    return ET.ElementTree(output_root)


def has_deleted_resources(tree: ET.ElementTree) -> bool:
    return any(resource_key(child) is not None for child in tree.getroot())


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


def upload_deleted_resources(file_path: Path, api_key: str) -> None:
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
        description="Diff two Android strings.xml files and upload the new local entries to Loco.",
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
        "--output",
        required=True,
        type=Path,
        help="Path where the diff XML payload uploaded to Loco is written.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the XML payload instead of uploading it.",
    )
    args = parser.parse_args()

    output_tree = build_deleted_resources(args.before, args.after)
    output_payload = serialize_tree(output_tree)

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(output_payload)

    if not has_deleted_resources(output_tree):
        print("No new local resources to upload.")
        return

    if args.dry_run:
        print(output_payload.decode("utf-8"))
        return

    try:
        upload_deleted_resources(args.output, args.api_key)
    except RuntimeError as error:
        print(f"❌ {error}", file=sys.stderr)
        sys.exit(1)
    print("Uploaded new local resources to Loco.")


if __name__ == "__main__":
    main()
