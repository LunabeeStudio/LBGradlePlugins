@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish")
}

dependencies {
    implementation(libs.gradleAndroid)
    implementation(libs.kotlinAndroid)
    implementation(libs.kotlinCompose)

    implementation(projects.lbPluginCore)
}

gradlePlugin {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.android.library") {
            id = "studio.lunabee.plugins.android.library"
            implementationClass = "studio.lunabee.plugins.LBAndroidLibraryPlugin"
            version = "1.0.0"
            displayName = "LBAndroidLibraryPlugin"
            description = "This plugin allows you to configure an Android library in a simple and fast way."
            tags = listOf("android", "library", "lunabee")
        }
    }
}
