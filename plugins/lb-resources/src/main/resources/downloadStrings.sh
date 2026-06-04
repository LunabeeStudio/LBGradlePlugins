#!/bin/bash

#
# Copyright (c) 2026 Lunabee Studio
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Created by Lunabee Studio / Date - 1/12/2026
# Last modified 12/17/25, 10:14 AM
#

LOCO_API_TOKEN=$1
PROJECT_LOCALIZABLE_FILES_ROOT_DIRECTORY=$2
STRING_FILENAME_WITHOUT_EXT=$3
REPLACE_APOSTROPHES=$4
REPLACE_QUOTES=$5

# Directory holding this script and the bundled python helpers.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is required by downloadStrings.sh but was not found on PATH." >&2
  exit 1
fi

EXTRACT_DIRECTORY_NAME="tmpextract"
LOCO_API_ARCHIVE_URL="https://localise.biz:443/api/export/archive/xml.zip"
DOWNLOAD_ZIP_DESTINATION="${EXTRACT_DIRECTORY_NAME}.zip"

function sedi() {
  # Handle Linux and MacOS (Darwin) sed command
  if [[ $(uname -s) == Linux ]]; then sed -i -r -- "$@"; else sed -i "" -E "$@"; fi
}

function import_language() {
  localizable_file=$1
  echo "Start import of $localizable_file"
  file="$localizable_file/$STRING_FILENAME_WITHOUT_EXT.xml"

  # Check for wrong java format specifiers
  bad_templates=$(grep "[^%]%[^abcdefghnostxABCEGHNSTX123456789%]" "$file")
  if [ -n "$bad_templates" ]; then
      printf "⛔️ Found strings with bad Java template in %s \n%s\n\n" "$localizable_file" "$bad_templates" >&2
      exit 1
  fi

  # Remove comment blocks
  sedi "/<\!--.*-->/d" "$file"
  sedi "/<"'!'"--/,/-->/d" "$file"

  # Add ignore unused resource lint check
  sedi 's/(<resources )/\1xmlns\:tools\=\"http\:\/\/schemas\.android\.com\/tools\" tools\:ignore\=\"UnusedResources\" /g' "$file"

  # Replace dot by underscore in string name's
  sedi ":loop
  s~(<(string|plurals) name=\".*)\\.(.*\">)~\1_\3~
  t loop" "$file"

  # Replace ... by …
  sedi "s~\.\.\.~…~g" "$file"

  # Replace x-x by x–x (where is x is a digit), except for url (string containing "http")
  sedi "\~http~!s~([0-9])-([0-9])~\1–\2~g" "$file"

  if [[ "$REPLACE_APOSTROPHES" == "true" ]]; then
    # Replace x\'x by x’x
    sedi "s~([^[:blank:]>])\\\'([^<[:blank:]])~\1’\2~g" "$file"

    # Replace others \' by ‘
    sedi "s~\\\'~‘~g" "$file"
  fi

  if [[ "$REPLACE_QUOTES" == "true" ]]; then
    # Replace \" by “
    # shellcheck disable=SC1111
    sedi "s~\\\\\"~“~g" "$file"
  fi

  # Duplicate missing plural forms (e.g. fr `many` from `other`). The python script only adds
  # a form when it is not already present, so complete plurals are left untouched (idempotent).
  # It's not clear what Android requires/supports for French:
  # https://issuetracker.google.com/issues?q=componentid:192718%20many%20french
  python3 "${SCRIPT_DIR}/duplicate_plural_forms.py" "$file" "$(basename "$localizable_file")"

  echo "Done import of $localizable_file"
}

pushd ./

# Download and unzip the strings archive.
curl "${LOCO_API_ARCHIVE_URL}?key=${LOCO_API_TOKEN}&index=id&format=android&status=translated,provisional&order=id" >"${DOWNLOAD_ZIP_DESTINATION}"
mkdir -p "${EXTRACT_DIRECTORY_NAME}"
unzip "${DOWNLOAD_ZIP_DESTINATION}" -d "${EXTRACT_DIRECTORY_NAME}"
MAIN_SUBDIRECTORY=$(ls -1rt "${EXTRACT_DIRECTORY_NAME}" | tail -1)

# Remove all txt files.
rm -f "${EXTRACT_DIRECTORY_NAME}/${MAIN_SUBDIRECTORY}"/*.txt

DESTINATION_DIRECTORY="${PROJECT_LOCALIZABLE_FILES_ROOT_DIRECTORY}"
mkdir -p "${DESTINATION_DIRECTORY}"

# Flag to track if any process failed
failed=false
# Array to store background process IDs
pids=()

start_time=$SECONDS
# Iterate over all languages directories to fix strings
for language_dir in "${EXTRACT_DIRECTORY_NAME}/${MAIN_SUBDIRECTORY}"/*; do
  for localizable_file in "$language_dir"/*; do
    import_language "$localizable_file" &
    pids+=($!)
  done
done

# Wait for all background processes to complete
for pid in "${pids[@]}"; do
    if ! wait "$pid"; then
        failed=true
    fi
done

wait # Wait all languages
elapsed=$((SECONDS - start_time))
echo "Import took $elapsed seconds"

cp -R "$language_dir" "${DESTINATION_DIRECTORY}/"

rm -Rf "${EXTRACT_DIRECTORY_NAME}"
rm -f "${DOWNLOAD_ZIP_DESTINATION}"

# Check if any process failed and exit with an error
if [ "$failed" = true ]; then
    echo "Download strings finish with error"
    exit 1
fi

popd || exit
