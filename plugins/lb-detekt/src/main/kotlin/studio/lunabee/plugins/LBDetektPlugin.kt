@file:Suppress("unused")

package studio.lunabee.plugins

import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
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

        target.pluginManager.apply(DetektPlugin::class.java)

        val extension = target.extensions.findByType(LBDetektExtension::class.java) ?: target.extensions.create(
            "lbDetekt",
            LBDetektExtension::class.java,
            target.extensions.getByType(DetektExtension::class.java)
        )
        extension.setup(target)

        target.dependencies.add(
            "detektPlugins",
            "io.gitlab.arturbosch.detekt:detekt-formatting:${extension.toolVersion.get()}",
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

    private fun LBDetektExtension.setup(project: Project) {

        lunabeeConfig.convention(project.rootProject.layout.buildDirectory.file("lbDetekt/detekt-config.yml"))
        verbose.convention(false)
        enableDependencySorting.convention(true)
        enableTomlSorting.convention(true)

        parallel.convention(true)
        buildUponDefaultConfig.convention(true)
        autoCorrect.convention(true)
        ignoreFailures.convention(true)

        config.setFrom(lunabeeConfig)
        source.setFrom("${project.rootProject.rootDir}")

        project.project.tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            doFirst {
                // Since Detekt only accepts a path to a configuration file as a parameter, we need to copy the detekt-config.yml
                // file from the plugin’s resources to the project using it. The Detekt plugin will then be able to access it.
                // We overwrite the file’s content each time to refresh it if needed (e.g., a plugin update).
                val lunabeeConfigFile = lunabeeConfig.get().asFile
                lunabeeConfigFile.apply {
                    delete()
                    parentFile?.mkdirs()
                    createNewFile()
                }

                lunabeeConfigFile.outputStream().use { outputStream ->
                    this::class.java.classLoader.getResource("detekt-config.yml")!!.openStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            if (enableDependencySorting.get()) {
                dependsOn(project.tasks.withType<SortBuildDependenciesTask>())
            }
            if (enableTomlSorting.get()) {
                dependsOn(project.tasks.withType<SortLibsVersionsTomlTask>())
            }

            reports {
                xml.required.set(true)
                val file = project.rootProject.layout.buildDirectory.file("/reports/detekt/detekt-report.xml")
                xml.outputLocation.set(file)
                html.required.set(true)
                html.outputLocation.set(project.rootProject.layout.buildDirectory.file("/reports/detekt/detekt-report.html"))
            }
        }

        project.project.tasks.withType<SortBuildDependenciesTask>().configureEach {
            verboseProp.set(verbose)
        }
    }
}
