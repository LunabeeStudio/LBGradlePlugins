# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2026-01-15

### LBAndroidFlavors 1.1.1

- Add `LBFlavorDimension` to expose build variants constant names

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
