@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
}

dependencies {
    implementation("com.android.tools.build:gradle:8.8.0")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")

    implementation(projects.lbPluginCore)
}

gradlePlugin {
    website = "https://studio.lunabee"
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

publishing {
    repositories {
        mavenLocal()
    }
}
