// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.gradle.plugin-publish") version "1.3.0" apply false
}

// Update gradle-wrapper by running `./gradlew wrapper --gradle-version latest`
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}