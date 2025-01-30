plugins {
    alias(libs.plugins.lbDokka) apply false
    alias(libs.plugins.lbDetekt)
}

// Update gradle-wrapper by running `./gradlew wrapper --gradle-version latest`
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

lbDetekt {
    verbose = true
}

subprojects {
    apply(plugin = "studio.lunabee.plugins.dokka")
}
