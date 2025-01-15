![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

# LBGradlePlugins

This repository contains a set of plugins useful for configuring our Android and KMP applications.

## Declare your plugins

Choose your plugin and refer to the corresponding `README.md` for more info about configuration:
| Plugin (id)                                   | Version                                                                   | Documentation                                 |
| -                                             | -                                                                         | -                                             |
| studio.lunabee.plugins.android.application    | ![LBAndroidApplication](https://img.shields.io/badge/latest-1.0.0-blue)   | [README.md](lb-android-application/README.md) |
| studio.lunabee.plugins.android.library        | ![LBAndroidLibrary](https://img.shields.io/badge/latest-1.0.0-blue)       | [README.md](lb-android-library/README.md)     |
| studio.lunabee.plugins.detekt                 | ![LBDetekt](https://img.shields.io/badge/latest-1.0.0-blue)               | [README.md](lb-detekt/README.md)              |
| studio.lunabee.plugins.resources              | ![LBResources](https://img.shields.io/badge/latest-1.0.0-blue)            | [README.md](lb-resources/README.md)           |
| studio.lunabee.plugins.multiplatform.library  | ![LBMultiplatformLibrary](https://img.shields.io/badge/latest-1.0.0-blue) | [README.md](lb-multiplatform-library/README.md)           |

## Publication

The plugins can be published locally or on Artifactory. During development, you simply need to run:

```bash
./gradlew mavenLocal
```

## Demo app

A demo app is provided with basic configuration that demonstrate how to configure and use each plugins.
