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

You can also synchronize strings with Loco (upload locally-deleted resources, then refresh strings):
```bash
./gradlew synchronizeStrings
```

The `synchronizeStrings` task requires `python3` to be available on `PATH`.

Note: currently, only Loco is handle as string provider.
