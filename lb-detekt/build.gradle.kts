@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
}

dependencies {
    implementation(libs.detekt)

    implementation(projects.lbPluginCore)

    testImplementation(libs.kotlinTest)
}

gradlePlugin {
    website = "https://studio.lunabee"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.detekt") {
            id = "studio.lunabee.plugins.detekt"
            implementationClass = "studio.lunabee.plugins.LBDetektPlugin"
            version = "1.0.0"
            displayName = "LBDetektPlugin"
            description = "This plugin allows you to configure Detekt to ensure consistent code style across all projects."
            tags = listOf("detekt", "android", "lunabee", "code", "style")
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}
