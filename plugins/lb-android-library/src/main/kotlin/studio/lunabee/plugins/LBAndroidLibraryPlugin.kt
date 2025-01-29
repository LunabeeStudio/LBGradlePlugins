@file:Suppress("unused")

package studio.lunabee.plugins

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType

/**
 * This plugin allows you to configure an Android library. Use it as follows:
 * ```
 * plugins {
 *     alias(libs.plugins.lbAndroidLibrary)
 * }
 *
 * lbAndroidLibrary {
 *     android {
 *         namespace = "my.application.namespace"
 *         // Set additional parameters here depending on your needs, see [LBAndroidLibraryExtension.PropertiesReceiver].
 *     }
 * }
 * ```
 */
class LBAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("lbAndroidLibrary", LBAndroidLibraryExtension::class.java)
        target.pluginManager.apply("com.android.library")
        val androidLibExt = target.extensions.getByType<LibraryExtension>()
        val javaExt = target.extensions.getByType<JavaPluginExtension>()

        // Apply Kotlin Android plugin if applicable.
        if (!target.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            target.pluginManager.apply("org.jetbrains.kotlin.android")
        }

        androidLibExt.compileSdk = 35
        androidLibExt.buildFeatures.buildConfig = false
        androidLibExt.buildToolsVersion = "35.0.1"
        androidLibExt.defaultConfig.minSdk = 23

        javaExt.toolchain { languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_17.toString())) }
    }
}
