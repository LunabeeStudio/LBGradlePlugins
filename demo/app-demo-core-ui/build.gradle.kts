plugins {
    alias(libs.plugins.lbAndroidLibrary)
}

lbAndroidLibrary {
    android {
        namespace = "studio.lunabee.plugin.demo.coreui"
        withCompose = true
    }
}

dependencies {
    implementation(platform(libs.androidxComposeBom))

    implementation(libs.androidxComposeFoundation)
    implementation(libs.androidxComposeMaterial3)
}
