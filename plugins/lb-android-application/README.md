# Module lb-android-application

### `studio.lunabee.plugins.android.application`

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

This plugin simplifies the configuration of an Android application. Usage:

In root `build.gradle.kts`:
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.lbAndroidApplication).apply(false)
}
```

In app `build.gradle.kts`:
```
plugins {
    alias(libs.plugins.lbAndroidApplication)
}

lbAndroidApplication {
    android {
        applicationId = "my.application.id"
        namespace = "my.application.namespace"
        compileSdk = 35
        minSdk = 26
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

After this configuration, you should be able to:
- use lint (depending on the configuration you provide).
- use Compose.
- access BuildConfig fields.
