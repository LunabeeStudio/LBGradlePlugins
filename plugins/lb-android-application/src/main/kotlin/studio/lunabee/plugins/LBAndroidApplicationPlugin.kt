@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This plugin allows you to configure your Android application. Generally, you will use it only in your app module as follows:
 * ```
 * plugins {
 *     alias(libs.plugins.lbAndroidApplication)
 * }
 *
 * lbAndroidApplication {
 *     android {
 *         applicationId = "my.application.id"
 *         namespace = "my.application.namespace"
 *         compileSdk = 35
 *         minSdk = 26
 *         versionCode = 102
 *         versionName = "6.0.0"
 *         // Set additional parameters here depending on your needs, see [LBAndroidApplicationExtension.PropertiesReceiver].
 *     }
 * }
 * ```
 */
class LBAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.android.application")
        target.pluginManager.apply("org.jetbrains.kotlin.android")
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        target.extensions.create("lbAndroidApplication", LBAndroidApplicationExtension::class.java)
    }
}
