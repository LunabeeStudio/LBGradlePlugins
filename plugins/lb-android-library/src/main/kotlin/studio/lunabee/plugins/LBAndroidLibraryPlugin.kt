@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

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
        target.applyPlugin("gradleAndroidLibrary")
    }
}
