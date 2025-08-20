@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("lunabee.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("studio.lunabee.plugins.cache") {
            id = "studio.lunabee.plugins.cache"
            implementationClass = "studio.lunabee.plugins.LBCachePlugin"
            version = "1.0.0"
            displayName = "LBCache"
            description = "This plugin allows you to configure the Lunabee remote Gradle cache"
            tags = listOf("gradle", "lunabee", "cache")
        }
    }
}
