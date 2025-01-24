@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradlePublish)
}

dependencies {
    implementation(libs.gradleAndroid)
    implementation(libs.kotlinAndroid)
}

gradlePlugin {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"

    plugins {
        create("studio.lunabee.plugins.android.flavors") {
            id = "studio.lunabee.plugins.android.flavors"
            implementationClass = "studio.lunabee.plugins.LBAndroidFlavorsPlugin"
            version = "1.0.0"
            displayName = "LBAndroidFlavors"
            description = "This plugin allows you to configure usual Android flavors in a simple and fast way."
            tags = listOf("android", "flavors", "lunabee")
        }
    }
}
