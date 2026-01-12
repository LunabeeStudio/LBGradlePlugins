# Module lb-cache

### `studio.lunabee.plugins.cache`

![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white)

This plugin allows you to configure the Lunabee remote Gradle cache. Usage:

In `settings.gradle.kts`:

```kotlin
plugins {
    id("studio.lunabee.plugins.cache") version [latest_version]
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