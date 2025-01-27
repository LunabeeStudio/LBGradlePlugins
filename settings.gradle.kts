@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        includeBuild("plugins")
    }
}

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.60.5"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
