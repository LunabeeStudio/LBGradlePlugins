# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2026-06-11

```
lb-resources v1.5.2
```

- `lb-resources`: `synchronizeStrings` no longer loses locally added strings when a step fails. The task downloads (overwriting the local files) before uploading, so newly added keys — absent from the provider — were dropped from disk and only recovered via the upload + re-download round trip; any failure in that chain left them lost and unpushed. Two safeguards: `downloadStrings.sh` now skips the copy and aborts **without** overwriting the working tree when any locale import fails, and `synchronizeStrings` rolls every working file back to its pre-run snapshot if the download or upload fails.

## 2026-06-10

```
lb-resources v1.5.1
```

- `lb-resources`: the Java format-specifier validator in `downloadStrings.sh` no longer rejects valid strings using a precision/width (e.g. `%.1f%%`). The allowed character set after `%` now includes `.` and `0`.

## 2026-06-04

```
lb-resources v1.5.0
```

- `lb-resources`: `synchronizeStrings` now synchronizes **every locale**, not just the default `values` directory. Each local `values*` directory containing a strings file is diffed against the freshly downloaded remote state and its new/modified entries are uploaded to Loco with the matching locale (`values` → `en`, `values-fr` → `fr`, `values-zh-rTW` → `zh-TW`, `values-b+zh+Hans` → `zh-Hans`). Previously only the default locale was pushed (hardcoded `locale=en`), so local translations added in `values-XX` files were never uploaded — and were silently wiped by the download step. Conflict detection/restore applies per locale with the same baseline rules.
- `lb-resources`: a modified key with no baseline entry (e.g. the key is not merged into the default branch yet) now keeps its **local value on disk** after the download, instead of being silently overwritten by the remote value. Loco is still left untouched for such keys, and a warning lists them.
- `lb-resources`: plural form duplication in `downloadStrings.sh` (e.g. fr `many` from `other`, ar `few`/`many`/`other` from `two`) is now **idempotent**: a form is only added when missing, instead of being blindly duplicated. Complete plurals coming from Loco are left untouched, which stops `synchronizeStrings` from flagging them as modified on every run. Duplicated forms are inserted in CLDR order (`few`, `many`, `other`). Implemented by the bundled `duplicate_plural_forms.py` (resolves the old sed TODO); `downloadStrings.sh` now requires `python3`.

## 2026-06-01

```
lb-resources v1.4.0
```

- `lb-resources`: `synchronizeStrings` now also uploads **modified** local strings to Loco, not just new ones. A modification is pushed only when it is safe: the local value differs from the baseline (the strings file on the repository's default branch, e.g. `origin/master`) and the remote value still matches that baseline. When both local and remote changed, the conflict is logged as a visible error, Loco is left untouched, and the dev's local value is restored on disk (so it is not lost to the download) while the other strings keep syncing. Because the baseline is the default branch, modifications are detected as long as they are not yet merged into it (commit freely on a feature branch). The baseline ref is configurable via `lbResources { baselineRef = "..." }`.

## 2026-05-26

```
lb-detekt v2.2.0
```

- Allow `.macos.kt` and `.apple.kt` suffixes

## 2026-05-13

```
lb-android-application v1.3.0
lb-resources v1.3.0
```

- Update compile SDK to 37
- `lb-resources`: add `synchronizeStrings` task that uploads deleted local resources to Loco then refreshes strings (requires `python3`)

## 2026-04-27

```
lb-detekt v2.1.2
```

- Bump detekt to 2.0.0-alpha.3

## 2026-04-02

```
lb-multiplatform-library v2.0.1-2.3.20
lb-multiplatform-android-library v1.0.1-2.3.20
lb-android-library v1.2.1
lb-android-flavors v1.2.1
lb-cache v1.0.1
lb-resources v1.2.1
lb-detekt v2.1.1
lb-dokka v1.0.1
lb-android-application v1.2.1
```

- Include Kotlin version in Kotlin-dependant plugins
- Bump all plugins to supersed versions *-2.3.10

## 2026-03-27

```
lb-multiplatform-library v2.0.1
lb-multiplatform-android-library v1.0.1
```

- fix: Update packages to disambiguate multiplatform and multiplatform-android `TargetPlatform` class

## 2026-03-20

### LBDetekt 2.1.0

- Add `studio.lunabee.detekt.skipDependencySorting` Gradle property to run Detekt without dependency sorting

## 2026-01-27

### LBDetekt 2.0.6

- Update Detekt to `2.0.0-alpha.2`

## 2026-01-16

```
lb-android-application v1.2.0
lb-android-flavors v1.2.0
lb-android-library v1.2.0
lb-multiplatform-library v2.0.0
lb-multiplatform-android-library v1.0.0
```

- Update AGP to 9.0.0
- **break** Migrate Android multiplatform plugin to `com.android.kotlin.multiplatform.library`
    - Add new plugin `lb-multiplatform-android-library` for KMP with Android target
    - Remove Android stuff from `lb-multiplatform-android-library`

## 2026-01-15

### LBAndroidFlavors 1.1.1

- Add `LBFlavorDimension` to expose build variants constant names

### LBDetekt 2.0.5

- Fix don't remove last line if dependency block is empty

## 2026-01-06

### LBDetekt 2.0.4

- Use `intellij_idea` code style instead of `ktlint_official` to fix indentation conflict with IDE

## 2025-12-17

### LBDetekt 2.0.3

- Ignore long method limit for previews

## 2025-11-21

- Add provisional status to download strings script

## 2025-11-19

### LBDetekt 2.0.2

- Disable `FunctionExpressionBody` and `FunctionExpressionBody`

## 2025-10-22

### LBDetekt 2.0.1

- Update detekt to `2.0.0-alpha.1`

## 2025-09-23

### LBDetekt 2.0.0

- Update detekt to `2.0.0-alpha.0`
- **break**: config use Gradle properties instead of Kotlin primitives

## 2025-08-20

### All

```
lb-android-application v1.1.0
lb-android-flavors v1.1.0
lb-android-library v1.1.0
lb-cache v1.0.0
lb-detekt v1.1.0
lb-dokka v1.0.0
lb-multiplatform-library v1.0.0
lb-resources v1.1.0
```

- **break**: Use JDK 21 by default

## 2025-06-03

### LBDetekt 1.0.5

- Ignore `LongParameterList` for `KoinViewModel` annotated classes

## 2025-06-03

### LBAndroidFlavors 1.0.0

- Fix (remove) version suffix for dev flavor (was not working)

### LBDetekt 1.0.4

- Handle `val` or `var` declaration in dependencies block by moving them to the top of the block

## 2025-05-26

### LBDetekt 1.0.3

- Ignore `LongParameterList` for `Entity` annotated class (Room)

## 2025-04-01

### LBAndroidApplication 1.0.0 & LBAndroidLibrary 1.0.0

- Remove buildToolsVersion
- Bump Android SDK to 36

## 2025-03-04

### LBDetekt 1.0.1

- Exclude `.idea` dir from detekt and re-add `buildSrc` dir

## 2025-02-21

### LBDetekt 1.0.0

- Fix sorting tasks not compatible with configuration cache

### LBResources 1.0.0

- Fix task not compatible with configuration cache

### Added

- LBAndroidApplication v0.9.X
- LBAndroidFlavors v0.9.X
- LBAndroidLibrary v0.9.X
- LBDetekt v0.9.X
- LBDokka v0.9.X
- LBMultiplatformLibrary v0.9.X
- LBResources v0.9.X
- LBCache v0.9.X
