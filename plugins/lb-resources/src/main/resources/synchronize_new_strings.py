#!/usr/bin/env python3

from __future__ import annotations

import argparse
import base64
import copy
import json
import shutil
import subprocess
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path


TOOLS_NAMESPACE = "http://schemas.android.com/tools"
XLIFF_NAMESPACE = "urn:oasis:names:tc:xliff:document:1.2"
LOCO_IMPORT_URL = "https://localise.biz/api/import/xml?index=id&locale=en&format=android"
SCRIPT_DIR = Path(__file__).resolve().parent
MODULE_DIR = SCRIPT_DIR.parent
KOTLIN_DIR = MODULE_DIR.parent.parent
GRADLEW_FILE = KOTLIN_DIR / "gradlew"
STRINGS_FILE = MODULE_DIR / "src/main/res/values/strings.xml"
OUTPUT_FILE = MODULE_DIR / "src/main/res/values/strings_to_upload.xml"
SNAPSHOT_FILE = MODULE_DIR / "build/tmp/downloadStringsKeepDeletedOnly/strings-before-download.xml"

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


def create_snapshot(snapshot_path: Path, strings_path: Path) -> None:
    snapshot_path.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(strings_path, snapshot_path)


def restore_snapshot(snapshot_path: Path, strings_path: Path) -> None:
    if snapshot_path.exists():
        shutil.copy2(snapshot_path, strings_path)


def cleanup_generated_files() -> None:
    for file_path in (OUTPUT_FILE, SNAPSHOT_FILE):
        if file_path.exists():
            file_path.unlink()


def run_download_strings(*, quiet: bool = False) -> None:
    command = [str(GRADLEW_FILE), ":core-res:downloadStrings"]
    if quiet:
        result = subprocess.run(
            command,
            cwd=KOTLIN_DIR,
            check=False,
            capture_output=True,
            text=True,
        )
        if result.returncode != 0:
            raise RuntimeError(
                "downloadStrings failed:\n"
                f"stdout:\n{result.stdout}\n"
                f"stderr:\n{result.stderr}",
            )
        return

    subprocess.run(command, cwd=KOTLIN_DIR, check=True)


def has_deleted_resources(tree: ET.ElementTree) -> bool:
    return any(resource_key(child) is not None for child in tree.getroot())


def serialize_tree(tree: ET.ElementTree) -> bytes:
    return ET.tostring(tree.getroot(), encoding="utf-8", xml_declaration=True)


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
        raise RuntimeError(f"Loco upload failed with HTTP {error.code}: {error_body}") from error
    except urllib.error.URLError as error:
        raise RuntimeError(f"Loco upload failed: {error.reason}") from error

    if not response_body:
        return

    payload = json.loads(response_body)
    if payload.get("error"):
        raise RuntimeError(f"Loco upload failed: {payload['error']}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Run downloadStrings, upload deleted resources to Loco, clean generated files, and refresh strings.",
    )
    parser.add_argument(
        "--api-key",
        required=True,
        help="Loco API key used to authenticate the upload.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the XML payload that would be uploaded and skip the upload.",
    )
    args = parser.parse_args()

    cleanup_generated_files()

    try:
        create_snapshot(SNAPSHOT_FILE, STRINGS_FILE)
        run_download_strings(quiet=args.dry_run)

        OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)
        output_tree = build_deleted_resources(SNAPSHOT_FILE, STRINGS_FILE)
        output_payload = serialize_tree(output_tree)
        OUTPUT_FILE.write_bytes(output_payload)

        if has_deleted_resources(output_tree):
            if args.dry_run:
                print(output_payload.decode("utf-8"))
                return

            upload_deleted_resources(OUTPUT_FILE, args.api_key)
            run_download_strings()
        elif args.dry_run:
            print("No strings would be uploaded.")
    finally:
        if args.dry_run:
            restore_snapshot(SNAPSHOT_FILE, STRINGS_FILE)
        cleanup_generated_files()


if __name__ == "__main__":
    main()
