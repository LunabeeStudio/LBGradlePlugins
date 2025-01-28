@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
    id("lunabee.plugin-publish-conventions")
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
        create("studio.lunabee.plugins.android.library") {
            id = "studio.lunabee.plugins.android.library"
            implementationClass = "studio.lunabee.plugins.LBAndroidLibraryPlugin"
            version = "0.9.0"
            displayName = "LBAndroidLibrary"
            description = "This plugin allows you to configure an Android library in a simple and fast way."
            tags = listOf("android", "library", "lunabee")
        }
    }
}
