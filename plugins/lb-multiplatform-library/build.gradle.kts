@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("lunabee.plugin-conventions")
}

dependencies {
    implementation(libs.gradleAndroid)
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.kotlinMultiplatform)
}

gradlePlugin {
    plugins {
        create("studio.lunabee.plugins.multiplatform.library") {
            id = "studio.lunabee.plugins.multiplatform.library"
            implementationClass = "studio.lunabee.plugins.LBMultiplatformLibraryPlugin"
            version = "0.9.0"
            displayName = "LBMultiplatformLibrary"
            description = "This plugin allows you to configure a multiplatform library in a simple and fast way."
            tags = listOf("android", "ios", "jvm", "multiplatform", "library", "lunabee")
        }
    }
}
