![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

# LBDokkaPlugin

This plugin applies Dokka to generate html documentation. Usage:

In [`gradle.properties`](https://github.com/LunabeeStudio/LBGradlePlugins/blob/master/gradle.properties), enable Dokka v2:

```
org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled
org.jetbrains.dokka.experimental.gradle.pluginMode.noWarn=true
```

In every documented module `build.gradle.kts`:

```
plugins {
    alias(libs.plugins.lbDokka)
}

dokka {
    // Dokka configuration
}
```

Create a `docs` module at the root:

```
plugins {
    alias(libs.plugins.lbDokka)
}

dokka {
    // Dokka configuration
}

dependencies {
    dokka(projects.mySubProject)
    // Add every modules participating to the doc
}
```

Add the application icon in `docs/images/logo-icon.svg`

After this configuration, you should be able to run

```bash
./gradlew docs:dokkaGenerate
```
