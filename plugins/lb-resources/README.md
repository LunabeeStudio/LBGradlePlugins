# Module lb-resources

### `studio.lunabee.plugins.resources`

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

This plugin allows you to download any resources for any provider configured. Usage:

In root `build.gradle.kts`:
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.lbResources)
}

lbResources {
    strings {
        providerKey = "myProviderKey"
        targetDirectory = "${project.rootDir.absolutePath}/app"
        provider = LBResourcesExtension.Provider.Loco
    }
}
```

After this configuration, you should be able to run:
```bash
./gradlew downloadStrings
```

You can also synchronize strings with Loco, then refresh strings:
```bash
./gradlew synchronizeStrings
```

`synchronizeStrings` uploads both **new** local strings (absent from Loco) and **modified** local strings.
A modification is pushed only when it is safe: the local value differs from the git `HEAD` baseline and the
remote value still matches that baseline. If both the local and the remote value changed, the conflict is
logged as a visible error, Loco is left untouched, and your local value is kept on disk (it is restored after
the download so it is not lost) while the other strings keep syncing.

Because the baseline is git `HEAD`, your local string modifications must be **uncommitted** for them to be
detected and pushed.

The `synchronizeStrings` task requires `python3` to be available on `PATH`.

Note: currently, only Loco is handle as string provider.
