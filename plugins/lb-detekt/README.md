# Module lb-detekt

### `studio.lunabee.plugins.detekt`

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

This plugin applies Detekt to analyze the code and ensure its quality. Usage:

In root [`build.gradle.kts`](https://github.com/LunabeeStudio/LBGradlePlugins/blob/master/build.gradle.kts):
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.lbDetekt)
}

lbDetekt {
    // custom configuration
}
```

After this configuration, you should be able to run
```bash
./gradlew detekt
```

To run Detekt without the dependency-sorting prerequisite, pass:
```bash
./gradlew detekt -Pstudio.lunabee.detekt.skipDependencySorting
```

### Lunabee custom rules

The plugin adds the `studio.lunabee.plugin.detekt:lb-detekt-rules` rule set (`lunabee`) to `detektPlugins`. Its rules are active by default.

#### `DispatcherSwitchOutsideDataLayer`

Reports `withContext`, `flowOn`, `launch` and `async` calls that switch to a background dispatcher (`Dispatchers.IO`, `Dispatchers.Default`, an injected `ioDispatcher`, …) from a presenter, reducer, use case, repository, view model, fragment, activity or composable. Datasources, DAOs and remote clients are [main-safe](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#main-safe): they switch the context themselves, so their callers never need to.

Classes are matched when their name contains one of the entries (`UserRepositoryImpl` matches `Repository`). Override the list, or scope the rule by path, in your project detekt config:

```yaml
lunabee:
  DispatcherSwitchOutsideDataLayer:
    forbiddenClassNameParts: [ 'Presenter', 'Reducer', 'UseCase', 'Repository', 'ViewModel', 'Fragment', 'Activity', 'Interactor' ]
    excludes: [ '**/test/**', '**/legacy/**' ]
```
