@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish")
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

publishing {
    repositories {
        mavenLocal()
    }
}
