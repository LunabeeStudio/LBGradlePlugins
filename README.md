# LBGradlePlugins

This repository contains a set of plugins useful for configuring our Android and KMP applications.

## Declare your plugins

Choose your plugin and refer to the corresponding section for more info about configuration:

In `libs.versions.toml`:
```
[versions]
lbAndroidApplication = "1.0.0"
lbDetekt = "1.0.0"

[plugins]
lbAndroidApplication = { id = "studio.lunabee.plugins.android.application", version.ref = "lbAndroidApplication" }
lbDetekt = { id = "studio.lunabee.plugins.detekt", version.ref = "lbDetekt" }
```

## LBAndroidApplicationPlugin

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

## LBDetektPlugin

This plugin applies Detekt to analyze the code and ensure its quality. Usage:

In root `build.gradle.kts`:
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.lbDetekt)
}

lbDetekt {
    configure()
}
```

After this configuration, you should be able to run
```bash
./gradlew detekt
```

## Publication

The plugins can be published locally or on Artifactory. During development, you simply need to run:

```bash
./gradlew mavenLocal
