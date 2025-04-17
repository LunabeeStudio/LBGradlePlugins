@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("lunabee.plugin-conventions")
}

dependencies {
    implementation(libs.detekt)

    testImplementation(libs.kotlinTest)
}

gradlePlugin {
    plugins {
        create("studio.lunabee.plugins.detekt") {
            id = "studio.lunabee.plugins.detekt"
            implementationClass = "studio.lunabee.plugins.LBDetektPlugin"
            version = "1.0.2"
            displayName = "LBDetekt"
            description = "This plugin allows you to configure Detekt to ensure consistent code style across all projects."
            tags = listOf("detekt", "android", "lunabee", "code", "style")
        }
    }
}
