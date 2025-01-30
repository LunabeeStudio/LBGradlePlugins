# Module lb-android-library

### `studio.lunabee.plugins.android.library`

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

This plugin simplifies the configuration of an Android library. Usage:

In root `build.gradle.kts`:
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.lbAndroidLibrary).apply(false)
}
```

In app `build.gradle.kts`:
```
plugins {
    alias(libs.plugins.lbAndroidLibrary)
}

lbAndroidLibrary {
    android {
        namespace = "my.application.namespace"
        withCompose = true
    }
}
```

After this configuration, you should be able to:
- use your module as an Android Library.
- use Compose, if enabled.
- access `BuildConfig` fields, if enabled.
