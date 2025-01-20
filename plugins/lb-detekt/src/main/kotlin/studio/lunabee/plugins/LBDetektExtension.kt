package studio.lunabee.plugins

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import studio.lunabee.plugins.LBDetektExtension.PropertiesReceiver
import java.io.File
import javax.inject.Inject

/**
 * This extension allows you to configure the LBDetekt plugin.
 * Use the [PropertiesReceiver] class to add optional configuration.
 */
open class LBDetektExtension @Inject constructor(private val project: Project) {

    // Register sort dependencies scripts.
    private val sortBuildDependenciesTask = registerSortDependencies()
    private val sortLibsVersionsTomlTask = registerSortLibs()

    private val propertiesReceiver = PropertiesReceiver()

    /**
     * @param projectSpecificConfigFilePath optional, null by default. Path to an additional configuration file for Detekt.
     * @param dependsOn Task to run before detekt. Default to [SortBuildDependenciesTask] and [SortLibsVersionsTomlTask].
     */
    inner class PropertiesReceiver(
        var projectSpecificConfigFilePath: String? = null,
        val dependsOn: MutableList<TaskProvider<out Task>> = mutableListOf(
            sortBuildDependenciesTask,
            sortLibsVersionsTomlTask,
        ),
    )

    /**
     * Must be called to configure Detekt plugin correctly, even if you don't customize it.
     * ```
     *  lbDetekt {
     *      configure()
     *  }
     * ```
     */
    fun configure(block: PropertiesReceiver.() -> Unit = { }) {
        block(propertiesReceiver)
        init()
    }

    private fun init() {
        // Since Detekt only accepts a path to a configuration file as a parameter, we need to copy the detekt-config.yml
        // file from the plugin’s resources to the project using it. The Detekt plugin will then be able to access it.
        // We overwrite the file’s content each time to refresh it if needed (e.g., a plugin update).
        val destFile = File(project.rootProject.layout.buildDirectory.asFile.get(), "lbDetekt/detekt-config.yml").apply {
            delete()
            parentFile.mkdirs()
            createNewFile()
        }
        destFile.outputStream().use { outputStream ->
            this::class.java.classLoader.getResource("detekt-config.yml")!!.openStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        project.extensions.configure<DetektExtension> {
            parallel = true
            buildUponDefaultConfig = true
            config.setFrom(listOfNotNull(destFile.absolutePath, propertiesReceiver.projectSpecificConfigFilePath))
            autoCorrect = true
            ignoreFailures = true
            source.setFrom("${project.rootProject.rootDir}")
        }

        project.project.tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            outputs.upToDateWhen { false } // always re-run

            exclude("**/buildSrc")
            exclude("**/build/**")

            propertiesReceiver.dependsOn.forEach { taskProvider ->
                dependsOn(taskProvider)
            }

            reports {
                xml.required.set(true)
                xml.outputLocation.set(File("${project.layout.buildDirectory.asFile.get().absolutePath}/reports/detekt/detekt-report.xml"))
                html.required.set(true)
                html.outputLocation.set(
                    File("${project.layout.buildDirectory.asFile.get().absolutePath}/reports/detekt/detekt-report.html"),
                )
            }
        }
    }

    private fun registerSortDependencies(): TaskProvider<SortBuildDependenciesTask> {
        return project.tasks.register<SortBuildDependenciesTask>("sortBuildDependencies") {
            group = "formatting"
            description = "Sorts dependencies and plugins alphabetically in all build.gradle.kts files throughout the project."
        }
    }

    private fun registerSortLibs(): TaskProvider<SortLibsVersionsTomlTask> {
        return project.tasks.register<SortLibsVersionsTomlTask>("sortLibsVersionsToml") {
            group = "formatting"
            description = "Sorts the libraries and versions in libs.versions.toml alphabetically"
        }
    }
}
