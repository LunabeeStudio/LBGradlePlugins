package studio.lunabee.plugins

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.withType
import java.io.File
import javax.inject.Inject

/**
 * This extension allows you to configure the LBDetekt plugin.
 *
 * @property verbose Enable debug logs. Default false
 * @property lunabeeConfig Default Lunabee configuration file
 * @property enableDependencySorting Run [SortBuildDependenciesTask] before detekt. Default true
 * @property enableTomlSorting Run [SortLibsVersionsTomlTask] before detekt. Default true
 */
open class LBDetektExtension @Inject constructor(
    objects: ObjectFactory,
    project: Project,
) : DetektExtension(objects) {
    var verbose: Boolean = false
    val lunabeeConfig = File(project.rootProject.layout.buildDirectory.asFile.get(), "lbDetekt/detekt-config.yml")
    var enableDependencySorting: Boolean = true
    var enableTomlSorting: Boolean = true

    init {
        parallel = true
        buildUponDefaultConfig = true
        autoCorrect = true
        ignoreFailures = true

        // Since Detekt only accepts a path to a configuration file as a parameter, we need to copy the detekt-config.yml
        // file from the plugin’s resources to the project using it. The Detekt plugin will then be able to access it.
        // We overwrite the file’s content each time to refresh it if needed (e.g., a plugin update).
        lunabeeConfig.apply {
            delete()
            parentFile.mkdirs()
            createNewFile()
        }

        lunabeeConfig.outputStream().use { outputStream ->
            this::class.java.classLoader.getResource("detekt-config.yml")!!.openStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        config.setFrom(lunabeeConfig)
        source.setFrom("${project.rootProject.rootDir}")

        project.project.tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            if (enableDependencySorting) {
                dependsOn(project.tasks.withType<SortBuildDependenciesTask>())
            }
            if (enableTomlSorting) {
                dependsOn(project.tasks.withType<SortLibsVersionsTomlTask>())
            }

            reports {
                xml.required.set(true)
                xml.outputLocation.set(File(project.layout.buildDirectory.asFile.get(), "/reports/detekt/detekt-report.xml"))
                html.required.set(true)
                html.outputLocation.set(File(project.layout.buildDirectory.asFile.get(), "/reports/detekt/detekt-report.html"))
            }
        }

        project.project.tasks.withType<SortBuildDependenciesTask>().configureEach {
            verboseProp.set(verbose)
        }
    }
}
