import studio.lunabee.plugins.LBResourcesExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.gradlePublish).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinCompose).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.gradleAndroidApplication).apply(false)
    alias(libs.plugins.gradleAndroidLibrary).apply(false)
    alias(libs.plugins.lbDetekt)
    alias(libs.plugins.lbResources)
}

// Update gradle-wrapper by running `./gradlew wrapper --gradle-version latest`
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

lbDetekt {
    configure {
        verbose = true
    }
}

lbResources {
    strings {
        providerKey = "maZteRAXR20kWa9XBlOfZ7Pgnwy8uor3"
        targetDirectory = "${project.rootDir.absolutePath}/demo/app"
        provider = LBResourcesExtension.Provider.Loco
    }
}
