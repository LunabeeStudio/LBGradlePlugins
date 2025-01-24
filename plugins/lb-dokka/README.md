![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

# LBDokkaPlugin

This plugin applies Dokka to analyze the code and ensure its quality. Usage:

In root [`build.gradle.kts`](https://github.com/LunabeeStudio/LBGradlePlugins/blob/master/build.gradle.kts):
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.lbDokka)
}

lbDokka {
    // custom configuration
}
```

After this configuration, you should be able to run
```bash
./gradlew dokka
```
