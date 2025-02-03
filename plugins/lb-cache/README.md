# Module lb-cache

### `studio.lunabee.plugins.cache`

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

This plugin allows you to configure the Lunabee remote Gradle cache. Usage:

In `settings.gradle.kts`:
```kotlin
pluginManagement {
    val artifactory_consumer_username: String? by settings
    val artifactory_consumer_api_key: String? by settings

    val artifactoryUsername: String = artifactory_consumer_username
        ?: "library-consumer-public"
    val artifactoryPassword: String = artifactory_consumer_api_key
        ?: "AKCp8k8PbuxYXoLgvNpc5Aro1ytENk3rSyXCwQ71BA4byg3h7iuMyQ6Sd4ZmJtSJcr7XjwMej"

    repositories {
        maven {
            url = uri("https://artifactory.lunabee.studio/artifactory/lunabee-gradle-plugin/")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                releasesOnly()
            }
        }
    }
}

plugins {
    id("studio.lunabee.plugins.cache") version "0.9.0"
}
```

In `gradle.properties` (project or user global):
```properties
# Mandatory to enable cache
org.gradle.caching=true

# Plugin optional properties
# Enable the local cache. Default true.
studio.lunabee.cacheEnableLocal=true
# Enable the remote cache. Default true.
studio.lunabee.cacheEnableRemote=true
# Push to cache server (for CI). Default false.
studio.lunabee.cacheIsPush=false
# Credentials for remote cache. Default to read-only credentials.
studio.lunabee.cacheUsername=lunabee
studio.lunabee.cachePassword=xxx
```