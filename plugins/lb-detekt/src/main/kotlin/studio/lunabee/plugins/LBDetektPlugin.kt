@file:Suppress("unused")

package studio.lunabee.plugins

import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
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
    override fun apply(project: Project) {
        // Register sort dependencies tasks.
        registerSortDependencies(project)
        registerSortLibs(project)

        // Apply original Detekt plugin
        project.pluginManager.apply(DetektPlugin::class.java)

        // Build custom Lunabee extension by implementing the DetektExtension interface with delegation to original Detekt extension
        val extension = project.extensions.findByType(DetektExtension::class.java)?.let {
            project.extensions.findByType(LBDetektExtension::class.java)
                ?: project.extensions.create("lbDetekt", LBDetektExtension::class.java, it)
        } ?: error("Cannot find detekt extension")

        // Apply default and custom values to the extension
        setupDetektPlugin(project, extension)

        // Add Detekt formatting plugin
        project.dependencies.add(
            "detektPlugins",
            "dev.detekt:detekt-rules-ktlint-wrapper:${extension.toolVersion.get()}",
        )

        // Configure Detekt task
        project.project.tasks.withType<dev.detekt.gradle.Detekt> {
            outputs.upToDateWhen { false } // always re-run

            exclude("**/.idea")
            exclude("**/build/**")
        }
    }

    /**
     * Register the [SortBuildDependenciesTask] to sort dependencies and plugins alphabetically
     */
    private fun registerSortDependencies(project: Project): TaskProvider<SortBuildDependenciesTask> = project.tasks
        .register<SortBuildDependenciesTask>(
            "sortBuildDependencies",
        ) {
            group = "Lunabee"
            description = "Sorts dependencies and plugins alphabetically in all build.gradle.kts files throughout the project"
        }

    /**
     * Register the [SortLibsVersionsTomlTask] to sort the libraries and versions in libs.versions.toml alphabetically
     */
    private fun registerSortLibs(project: Project): TaskProvider<SortLibsVersionsTomlTask> = project.tasks
        .register<SortLibsVersionsTomlTask>(
            "sortLibsVersionsToml",
        ) {
            group = "Lunabee"
            description = "Sorts the libraries and versions in libs.versions.toml alphabetically"
        }

    /**
     * Apply default and custom values to the extension
     */
    private fun setupDetektPlugin(project: Project, extension: LBDetektExtension) {
        with(extension) {
            verbose.set(false)
            lunabeeConfig.set(
                project.rootProject.layout.buildDirectory
                    .file("lbDetekt/detekt-config.yml"),
            )
            enableDependencySorting.set(true)
            enableTomlSorting.set(true)

            parallel.set(true)
            buildUponDefaultConfig.set(true)
            autoCorrect.set(true)
            ignoreFailures.set(true)

            // Since Detekt only accepts a path to a configuration file as a parameter, we need to copy the detekt-config.yml
            // file from the plugin’s resources to the project using it. The Detekt plugin will then be able to access it.
            // We overwrite the file’s content each time to refresh it if needed (e.g., a plugin update).
            val configFile = lunabeeConfig.get().asFile
            configFile.apply {
                delete()
                parentFile.mkdirs()
                createNewFile()
            }

            configFile.outputStream().use { outputStream ->
                this::class.java.classLoader.getResource("detekt-config.yml")!!.openStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            config.setFrom(lunabeeConfig)
            source.setFrom(project.files(project.rootProject.rootDir))

            project.project.tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
                if (enableDependencySorting.get()) {
                    dependsOn(project.tasks.withType<SortBuildDependenciesTask>())
                }
                if (enableTomlSorting.get()) {
                    dependsOn(project.tasks.withType<SortLibsVersionsTomlTask>())
                }

                reports {
                    xml.required.set(true)
                    xml.outputLocation.set(project.layout.buildDirectory.file("reports/detekt/detekt-report.xml"))

                    html.required.set(true)
                    html.outputLocation.set(project.layout.buildDirectory.file("reports/detekt/detekt-report.html"))
                }
            }

            project.project.tasks.withType<SortBuildDependenciesTask>().configureEach {
                verboseProp.set(verbose)
            }
        }
    }
}
