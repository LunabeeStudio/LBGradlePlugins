![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

# LBDetektPlugin

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
