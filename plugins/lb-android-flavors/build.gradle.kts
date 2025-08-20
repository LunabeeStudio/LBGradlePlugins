@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("lunabee.plugin-conventions")
}

dependencies {
    implementation(libs.gradleAndroid)
    implementation(libs.kotlinGradlePlugin)
}

gradlePlugin {
    plugins {
        create("studio.lunabee.plugins.android.flavors") {
            id = "studio.lunabee.plugins.android.flavors"
            implementationClass = "studio.lunabee.plugins.LBAndroidFlavorsPlugin"
            version = "1.1.0"
            displayName = "LBAndroidFlavors"
            description = "This plugin allows you to configure usual Android flavors in a simple and fast way."
            tags = listOf("android", "flavors", "lunabee")
        }
    }
}
