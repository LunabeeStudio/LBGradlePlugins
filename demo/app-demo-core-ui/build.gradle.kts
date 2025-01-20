plugins {
    alias(libs.plugins.lbAndroidLibrary)
}

lbAndroidLibrary {
    android {
        namespace = "my.application.namespace"
        withCompose = true
    }
}

dependencies {
    implementation(platform(libs.androidxComposeBom))

    implementation(libs.androidxComposeFoundation)
    implementation(libs.androidxComposeMaterial3)
}
