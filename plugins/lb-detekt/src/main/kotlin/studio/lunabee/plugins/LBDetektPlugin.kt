@file:Suppress("unused")

package studio.lunabee.plugins

import io.gitlab.arturbosch.detekt.DetektPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

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
 *     // custom configuration
 * }
 * ```
 */
class LBDetektPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Register sort dependencies tasks.
        registerSortDependencies(target)
        registerSortLibs(target)

        val extension = target.extensions.findByType(LBDetektExtension::class.java) ?: target.extensions.create(
            "lbDetekt",
            LBDetektExtension::class.java,
        )
        target.pluginManager.apply(DetektPlugin::class.java)
        target.dependencies.add(
            "detektPlugins",
            "io.gitlab.arturbosch.detekt:detekt-formatting:${extension.toolVersion}",
        )

        target.project.tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            outputs.upToDateWhen { false } // always re-run

            exclude("**/buildSrc")
            exclude("**/build/**")
        }
    }

    private fun registerSortDependencies(project: Project): TaskProvider<SortBuildDependenciesTask> {
        return project.tasks.register<SortBuildDependenciesTask>("sortBuildDependencies") {
            group = "Lunabee"
            description = "Sorts dependencies and plugins alphabetically in all build.gradle.kts files throughout the project"
        }
    }

    private fun registerSortLibs(project: Project): TaskProvider<SortLibsVersionsTomlTask> {
        return project.tasks.register<SortLibsVersionsTomlTask>("sortLibsVersionsToml") {
            group = "Lunabee"
            description = "Sorts the libraries and versions in libs.versions.toml alphabetically"
        }
    }
}
