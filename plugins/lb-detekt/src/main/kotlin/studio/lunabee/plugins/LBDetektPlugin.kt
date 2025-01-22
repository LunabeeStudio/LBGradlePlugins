@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This plugin initializes and configures the Detekt plugin. Generally, we recommend applying this plugin directly in the
 * `build.gradle.kts` file located at the root of your project.
 * ```
 * // Top-level build file where you can add configuration options common to all sub-projects/modules.
 * plugins {
 *     alias(libs.plugins.lbDetekt)
 * }
 *
 * lbDetekt {
 *     settings()
 * }
 * ```
 */
class LBDetektPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("lbDetekt", LBDetektExtension::class.java)
        target.applyPlugin("detekt")
        target.dependencies.add("detektPlugins", target.buildDependency("detektFormatting"))
    }
}
