@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("lunabee.plugin-conventions")
}

dependencies {
    implementation(libs.dokkaGradlePlugin)
}

gradlePlugin {
    plugins {
        create("studio.lunabee.plugins.dokka") {
            id = "studio.lunabee.plugins.dokka"
            implementationClass = "studio.lunabee.plugins.LBDokkaPlugin"
            version = "0.9.1"
            displayName = "LBDokka"
            description = "This plugin allows you to configure Dokka to ensure consistent doc generation across all projects."
            tags = listOf("dokka", "android", "lunabee", "doc")
        }
    }
}
