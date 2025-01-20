@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
}

gradlePlugin {
    website = "https://studio.lunabee"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.resources") {
            id = "studio.lunabee.plugins.resources"
            implementationClass = "studio.lunabee.plugins.LBResourcesPlugin"
            version = "1.0.0"
            displayName = "LBResourcesPlugin"
            description = "This plugin allows you to configure a task to download resources from any provider."
            tags = listOf("android", "lunabee", "resources")
        }
    }
}

publishing {
    repositories {
        // mavenLocal()
    }
}
