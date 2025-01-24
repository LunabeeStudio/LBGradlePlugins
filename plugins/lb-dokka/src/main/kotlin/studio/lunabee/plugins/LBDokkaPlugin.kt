@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.dokka.gradle.DokkaPlugin

/**
 * This plugin initializes and configures the Dokka plugin.
 * ```
 * // Top-level build file where you can add configuration options common to all sub-projects/modules.
 * plugins {
 *     alias(libs.plugins.lbDokka)
 * }
 *
 * lbDokka {
 *     // custom configuration
 * }
 * ```
 */
class LBDokkaPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(DokkaPlugin::class.java)
        target.extensions.findByType(LBDokkaExtension::class.java) ?: target.extensions.create(
            "lbDokka",
            LBDokkaExtension::class.java,
        )
    }
}
