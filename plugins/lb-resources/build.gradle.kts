@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("lunabee.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("studio.lunabee.plugins.resources") {
            id = "studio.lunabee.plugins.resources"
            implementationClass = "studio.lunabee.plugins.LBResourcesPlugin"
            version = "1.1.0"
            displayName = "LBResources"
            description = "This plugin allows you to configure a task to download resources from any provider."
            tags = listOf("android", "lunabee", "resources")
        }
    }
}
