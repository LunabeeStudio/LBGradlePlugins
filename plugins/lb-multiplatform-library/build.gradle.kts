@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
}

dependencies {
    implementation(libs.gradleAndroid)
    implementation(libs.kotlinAndroid)
    implementation(libs.kotlinMultiplatform)
}

gradlePlugin {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.multiplatform.library") {
            id = "studio.lunabee.plugins.multiplatform.library"
            implementationClass = "studio.lunabee.plugins.LBMultiplatformLibraryPlugin"
            version = properties["lunabee.LBMultiplatformLibrary.version"]!!
            displayName = "LBMultiplatformLibrary"
            description = "This plugin allows you to configure a multiplatform library in a simple and fast way."
            tags = listOf("android", "ios", "jvm", "multiplatform", "library", "lunabee")
        }
    }
}
