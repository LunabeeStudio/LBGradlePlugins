plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gradlePublishPlugin)
    implementation(libs.kotlinGradlePlugin)
}
