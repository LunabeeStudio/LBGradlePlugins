@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        includeBuild("plugins")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("studio.lunabee.plugins.cache") version "0.9.0"
}

rootProject.name = "LBGradlePluginsDemo"
include(":app")
project(":app").projectDir = File("./demo/app")
include(":app-demo-core-ui")
project(":app-demo-core-ui").projectDir = File("./demo/app-demo-core-ui")
include(":app-demo-multiplatform")
project(":app-demo-multiplatform").projectDir = File("./demo/app-demo-multiplatform")
include(":app-demo-shared")
project(":app-demo-shared").projectDir = File("./demo/app-demo-shared")
include(":docs")
