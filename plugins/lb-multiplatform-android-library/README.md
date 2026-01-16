# Module lb-multiplatform-android-library

### `studio.lunabee.plugins.multiplatform.android.library`

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

This plugin simplifies the configuration of an Android multiplatform library. Usage:

In root `build.gradle.kts`:
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.lbMultiplatformAndroidLibrary).apply(false)
}
```

In app `build.gradle.kts`:
```
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import studio.lunabee.plugins.TargetPlatform
plugins {
    alias(libs.plugins.lbMultiplatformAndroidLibrary)
}

lbAndroidLibrary {
    android {
        namespace = "my.application.namespace"
        withCompose = false
    }
}

lbMultiplatformLibrary {
    multiplatform {
        jvmTarget = JvmTarget.JVM_21
        targets = listOf(
            TargetPlatform.Android(),
            TargetPlatform.Jvm(),
            TargetPlatform.Ios {
                // Optional configuration for iOS (export...)
            },
        )
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // Android dependencies goes here.
        }
        commonMain.dependencies {
            // Common dependencies goes here.
        }
        iosMain.dependencies {
            // iOS dependencies goes here.
        }
        jvmMain.dependencies {
            // Jvm dependencies goes here.
        }
    }
}
```
