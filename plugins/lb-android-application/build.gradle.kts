@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
}

dependencies {
    implementation(libs.gradleAndroid)
    implementation(libs.kotlinAndroid)
    implementation(libs.kotlinCompose)
}

gradlePlugin {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.android.application") {
            id = "studio.lunabee.plugins.android.application"
            implementationClass = "studio.lunabee.plugins.LBAndroidApplicationPlugin"
            version = "1.0.0"
            displayName = "LBAndroidApplication"
            description = "This plugin allows you to configure an Android application in a simple and fast way."
            tags = listOf("android", "application", "lunabee")
        }
    }
}
