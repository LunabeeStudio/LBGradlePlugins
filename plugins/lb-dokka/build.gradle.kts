@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
}

dependencies {
    implementation(libs.dokkaGradlePlugin)
}

gradlePlugin {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.dokka") {
            id = "studio.lunabee.plugins.dokka"
            implementationClass = "studio.lunabee.plugins.LBDokkaPlugin"
            version = "1.0.0"
            displayName = "LBDokkaPlugin"
            description = "This plugin allows you to configure Dokka to ensure consistent code style across all projects."
            tags = listOf("dokka", "android", "lunabee", "doc")
        }
    }
}
