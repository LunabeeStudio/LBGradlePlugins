plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.gradle.plugin-publish:com.gradle.plugin-publish.gradle.plugin:1.3.0")
}
