plugins {
    alias(libs.plugins.lbAndroidApplication)
}

lbAndroidApplication {
    android {
        applicationId = "studio.lunabee.lbplugin"
        namespace = "studio.lunabee.lbplugin"
        compileSdk = 35
        minSdk = 26
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(platform(libs.androidxComposeBom))

    implementation(libs.androidxActivityCompose)
    implementation(libs.androidxAppCompat)
    implementation(libs.androidxComposeFoundation)
    implementation(libs.androidxComposeMaterial3)

    implementation(projects.appDemoCoreUi)
    implementation(projects.appDemoMultiplatform)
    implementation(projects.appDemoShared)
}
