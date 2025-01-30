@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("lunabee.plugin-conventions")
}

dependencies {
    implementation(libs.gradleAndroid)
    implementation(libs.kotlinCompose)
    implementation(libs.kotlinGradlePlugin)
}

gradlePlugin {
    plugins {
        create("studio.lunabee.plugins.android.application") {
            id = "studio.lunabee.plugins.android.application"
            implementationClass = "studio.lunabee.plugins.LBAndroidApplicationPlugin"
            version = "0.9.1"
            displayName = "LBAndroidApplication"
            description = "This plugin allows you to configure an Android application in a simple and fast way."
            tags = listOf("android", "application", "lunabee")
        }
    }
}
