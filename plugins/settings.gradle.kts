@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://artifactory.lunabee.studio/artifactory/lunabee-gradle-plugin/")
            credentials {
                username = "library-consumer-public"
                password = "AKCp8k8PbuxYXoLgvNpc5Aro1ytENk3rSyXCwQ71BA4byg3h7iuMyQ6Sd4ZmJtSJcr7XjwMej"
            }
            mavenContent {
                releasesOnly()
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "LBGradlePlugins"
include(":docs")
include(":lb-android-application")
include(":lb-android-library")
include(":lb-android-flavors")
include(":lb-detekt")
include(":lb-dokka")
include(":lb-multiplatform-library")
include(":lb-resources")
