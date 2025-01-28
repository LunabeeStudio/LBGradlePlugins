@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
    id("lunabee.plugin-publish-conventions")
}

gradlePlugin {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.resources") {
            id = "studio.lunabee.plugins.resources"
            implementationClass = "studio.lunabee.plugins.LBResourcesPlugin"
            version = "0.9.0"
            displayName = "LBResources"
            description = "This plugin allows you to configure a task to download resources from any provider."
            tags = listOf("android", "lunabee", "resources")
        }
    }
}
