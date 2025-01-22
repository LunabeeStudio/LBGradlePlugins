#!/bin/bash

LOCO_API_TOKEN=$1
PROJECT_LOCALIZABLE_FILES_ROOT_DIRECTORY=$2
STRING_FILENAME_WITHOUT_EXT=$3

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

  # Replace x\'x by x’x
  sedi "s~([^[:blank:]>])\\\'([^<[:blank:]])~\1’\2~g" "$file"

  # Replace others \' by ‘
  sedi "s~\\\'~‘~g" "$file"

  # Replace \" by “
  sedi "s~\\\\\"~“~g" "$file"

  # TODO for plurals form duplication, we should check if the form already exist before duplicating (switch to python)

  if [[ "$localizable_file" == *values-ar ]]; then
    echo "Duplicate plural form two to few for $localizable_file"
    sedi "s~(.*<item quantity=\")(two)(.*)~\1\2\3\n\1few\3~g" "$file"
    echo "Duplicate plural form two to many for $localizable_file"
    sedi "s~(.*<item quantity=\")(two)(.*)~\1\2\3\n\1many\3~g" "$file"
    echo "Duplicate plural form two to other for $localizable_file"
    sedi "s~(.*<item quantity=\")(two)(.*)~\1\2\3\n\1other\3~g" "$file"
  fi

  # It's not clear what Android required/support for French
  # https://issuetracker.google.com/issues?q=componentid:192718%20many%20french
  if [[ "$localizable_file" == *values-fr ||
    "$localizable_file" == *values-es ||
    "$localizable_file" == *values-pt ||
    "$localizable_file" == *values-it ]] \
    ; then
    echo "Duplicate plural form other to many for $localizable_file"
    sedi "s~(.*<item quantity=\")(other)(.*)~\1\2\3\n\1many\3~g" "$file"
  fi

  if [[ "$localizable_file" == *values-pl ||
    "$localizable_file" == *values-ru ||
    "$localizable_file" == *values-uk ]] \
    ; then
    echo "Duplicate plural form few to many for $localizable_file"
    sedi "s~(.*<item quantity=\")(few)(.*)~\1\2\3\n\1many\3~g" "$file"
  fi

  echo "Done import of $localizable_file"
}

pushd ./

# Download and unzip the strings archive.
curl "${LOCO_API_ARCHIVE_URL}?key=${LOCO_API_TOKEN}&index=id&format=android&status=translated&order=id" >"${DOWNLOAD_ZIP_DESTINATION}"
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
