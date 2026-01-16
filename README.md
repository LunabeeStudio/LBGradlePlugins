![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

# LBGradlePlugins

This repository contains a set of plugins useful for configuring our Android and KMP applications.

## Declare your plugins

Choose your plugin and refer to the corresponding `README.md` for more info about configuration:
| Plugin (id)                                           | Documentation                                                     |
| -                                                     | -                                                                 |
| studio.lunabee.plugins.android.application            | [README.md](plugins/lb-android-application/README.md)             |
| studio.lunabee.plugins.android.flavors                | [README.md](plugins/lb-android-flavors/README.md)                 |
| studio.lunabee.plugins.android.library                | [README.md](plugins/lb-android-library/README.md)                 |
| studio.lunabee.plugins.multiplatform.library          | [README.md](plugins/lb-multiplatform-library/README.md)           |
| studio.lunabee.plugins.multiplatform.android.library  | [README.md](plugins/lb-multiplatform-android-library/README.md)   |
| studio.lunabee.plugins.cache                          | [README.md](plugins/lb-cache/README.md)                           |
| studio.lunabee.plugins.detekt                         | [README.md](plugins/lb-detekt/README.md)                          |
| studio.lunabee.plugins.dokka                          | [README.md](plugins/lb-dokka/README.md)                           |
| studio.lunabee.plugins.resources                      | [README.md](plugins/lb-resources/README.md)                       |

## Local deployment

```bash
cd plugins
./gradlew publishToMavenLocal
```

## Demo app

A demo app is provided with basic configuration that demonstrate how to configure and use each plugins.
