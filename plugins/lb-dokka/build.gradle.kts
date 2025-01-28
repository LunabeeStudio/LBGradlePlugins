@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
    id("lunabee.plugin-publish-conventions")
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
            version = properties["lunabee.LBDokka.version"]!!
            displayName = "LBDokka"
            description = "This plugin allows you to configure Dokka to ensure consistent doc generation across all projects."
            tags = listOf("dokka", "android", "lunabee", "doc")
        }
    }
}
