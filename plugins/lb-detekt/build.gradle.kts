@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
    id("lunabee.plugin-publish-conventions")
}

dependencies {
    implementation(libs.detekt)

    testImplementation(libs.kotlinTest)
}

gradlePlugin {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.detekt") {
            id = "studio.lunabee.plugins.detekt"
            implementationClass = "studio.lunabee.plugins.LBDetektPlugin"
            version = "0.9.0"
            displayName = "LBDetekt"
            description = "This plugin allows you to configure Detekt to ensure consistent code style across all projects."
            tags = listOf("detekt", "android", "lunabee", "code", "style")
        }
    }
}
