# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Release Rules

- Bump the minor version when a change adds a new feature.
- Bump the patch version when a change is fix-only.
- Follow Semantic Versioning for all version changes.
- Do not bump plugin versions in `gradle/libs.versions.toml`; that file is used by the demo and is updated by Renovate after release.

## Changelog Rules

- Update `CHANGELOG.MD` for every version change.
- Add a clear entry that matches the released version and summarizes the change.

## Documentation Rules

- Update the relevant `README.md` files whenever usage changes.

## Repository layout

Two distinct Gradle builds live side-by-side, with the root build consuming the inner one via `includeBuild`:

- **`plugins/`** — the actual Gradle plugin sources published to Maven Central. Standalone Gradle build (`plugins/settings.gradle.kts`) with one subproject per plugin (`lb-android-application`, `lb-android-library`, `lb-android-flavors`, `lb-multiplatform-library`, `lb-multiplatform-android-library`, `lb-cache`, `lb-detekt`, `lb-dokka`, `lb-resources`, plus a `docs` aggregator).
- **`demo/`** — sample Android/KMP apps wired into the root `settings.gradle.kts` that consume the plugins. The root build is the demo build; running `./gradlew` from the repo root targets these apps.
- **`plugins/buildSrc/`** — single convention plugin `lunabee.plugin-conventions` applied by every plugin module. It centralizes JReleaser + Maven Central publishing, signing, Gradle Plugin Portal publishing, and POM metadata. Each plugin module only declares its `gradlePlugin { plugins { create(...) } }` block, its `description`, and its `group` — version lives there too (not in `libs.versions.toml`).
- **`gradle/libs.versions.toml`** — shared version catalog. Imported by both builds (the inner `plugins/settings.gradle.kts` reads it via `from(files("../gradle/libs.versions.toml"))`). Plugin versions listed in `[versions]` describe what the *demo* consumes, not what the plugin publishes.

## Common commands

Build & publish plugins locally (most common workflow when iterating on a plugin and testing it from the demo):

```bash
cd plugins
./gradlew publishToMavenLocal
```

Run plugin tests (only `lb-detekt` has tests currently):

```bash
cd plugins
./gradlew :lb-detekt:test
./gradlew :lb-detekt:test --tests "studio.lunabee.plugins.DependencyComparatorTest"
```

Run detekt on the demo build (uses the local `lb-detekt` plugin):

```bash
./gradlew detekt
```

Build the demo apps (validates the plugins end-to-end):

```bash
./gradlew :app:assembleDebug
./gradlew :app-demo-multiplatform:assemble
```

Print a plugin's published coordinates:

```bash
cd plugins
./gradlew :lb-detekt:PrintCoordinates
```

Update the Gradle wrapper (both builds):

```bash
./gradlew wrapper --gradle-version latest
cd plugins && ./gradlew wrapper --gradle-version latest
```

## Releasing to Maven Central

`buildSrc` configures JReleaser. The flow is: each plugin module publishes to a per-module `build/staging-deploy` Maven repo, then JReleaser uploads that staging dir to Sonatype Central. Required Gradle properties: `mavenCentralUsername`, `mavenCentralPassword`, `signingKey`, `signingPassword`. Signing is skipped automatically for `publishToMavenLocal`.

## Versioning conventions

- Each plugin's published version is hard-coded inside its module `build.gradle.kts` under `gradlePlugin { plugins { create(...) { version = "..." } } }`. There is **no single source of truth** — bumping a plugin means editing that file and updating `CHANGELOG.MD`.
- KMP plugins (`lb-multiplatform-library`, `lb-multiplatform-android-library`) embed the Kotlin version in their plugin version (e.g. `2.0.1-2.3.21`) because they expose Kotlin compiler APIs and must be ABI-matched to the consumer's Kotlin version.
- After bumping, update `libs.versions.toml` only if you want the demo to consume the new version.

## Plugin architecture pattern

Each Lunabee plugin follows the same shape:
- `LB<Name>Plugin.kt` implements `Plugin<Project>` (or `Plugin<Settings>` for `lb-cache`) and registers an extension + configures the relevant Android/Kotlin DSL on `afterEvaluate` or via the variant API.
- `LB<Name>Extension.kt` exposes user-facing config (Gradle properties / providers, not raw Kotlin primitives — this was a breaking change in detekt 2.0.0).
- Tasks (e.g. `SortBuildDependenciesTask`, `DownloadStringsTask`, `GetGitInfoTask`) must stay **configuration-cache compatible** — avoid capturing `Project` in task actions, use `@Input`/`@InputFiles`/providers instead. This was explicitly fixed in `lb-detekt` 1.0.0 and `lb-resources` 1.0.0 and regressions are easy.

## Notes for changes

- Build wrappers exist at the repo root **and** in `plugins/`. When in doubt about which build a command targets, check `pwd` — `./gradlew` from root runs the demo build; from `plugins/` runs the plugin build.
- `TYPESAFE_PROJECT_ACCESSORS` is enabled in both settings files — use `projects.lbDetekt`-style accessors instead of `project(":lb-detekt")` when wiring cross-module deps.
- Configuration cache is enabled (`org.gradle.configuration-cache=true`); test any new task with a second invocation to confirm the cache reuses the entry.
- Detekt v2 migration is in progress (`2.0.0-alpha.3`). The `lb-detekt` plugin's config uses Gradle properties, not Kotlin primitives.
