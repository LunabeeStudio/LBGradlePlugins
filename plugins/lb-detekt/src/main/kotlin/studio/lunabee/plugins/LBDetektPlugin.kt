@file:Suppress("unused")

package studio.lunabee.plugins

import io.gitlab.arturbosch.detekt.DetektPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

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
        // Register sort dependencies scripts.
        registerSortDependencies(target)
        registerSortLibs(target)

        target.extensions.findByType(LBDetektExtension::class.java) ?: target.extensions.create(
            "lbDetekt",
            LBDetektExtension::class.java,
        )
        target.pluginManager.apply(DetektPlugin::class.java)
        target.dependencies.add("detektPlugins", target.buildDependency("detektFormatting"))
    }

    private fun registerSortDependencies(project: Project): TaskProvider<SortBuildDependenciesTask> {
        return project.tasks.register<SortBuildDependenciesTask>("sortBuildDependencies") {
            group = "formatting"
            description = "Sorts dependencies and plugins alphabetically in all build.gradle.kts files throughout the project."
        }
    }

    private fun registerSortLibs(project: Project): TaskProvider<SortLibsVersionsTomlTask> {
        return project.tasks.register<SortLibsVersionsTomlTask>("sortLibsVersionsToml") {
            group = "formatting"
            description = "Sorts the libraries and versions in libs.versions.toml alphabetically"
        }
    }
}
