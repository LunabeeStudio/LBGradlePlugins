@file:Suppress("unused")

package studio.lunabee.plugins

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import java.io.File
import javax.inject.Inject

/**
 * This extension allows you to configure the LBDetekt plugin.
 * Use the [PropertiesReceiver] class to add optional configuration.
 */
open class LBDetektExtension @Inject constructor(private val project: Project) {
    private val propertiesReceiver = PropertiesReceiver()

    /**
     * @param projectSpecificConfigFilePath optional, null by default. Path to an additional configuration file for Detekt.
     * @param dependsOn optional, default to empty. Add custom tasks name that detekt will depend on.
     */
    data class PropertiesReceiver(
        var projectSpecificConfigFilePath: String? = null,
        var dependsOn: List<String> = emptyList(),
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
        val configFile = this::class.java.classLoader.getResource("detekt-config.yml")!!.readText()
        val destFolder = File("${project.rootProject.layout.buildDirectory.asFile.get().absolutePath}/lbDetekt")
        if (!destFolder.exists()) destFolder.mkdirs()
        // We overwrite the file’s content each time to refresh it if needed (e.g., a plugin update).
        val destFile = File(destFolder, "detekt-config.yml")
        if (destFile.exists()) destFile.delete()
        destFile.createNewFile()
        destFile.writeText(configFile)

        project.extensions.configure<DetektExtension> {
            parallel = true
            buildUponDefaultConfig = true
            config.setFrom(listOfNotNull(destFile.absolutePath, propertiesReceiver.projectSpecificConfigFilePath))
            autoCorrect = true
            ignoreFailures = true
            source.setFrom("${project.rootProject.rootDir}")
        }

        project.project.tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            exclude("**/buildSrc")
            exclude("**/build/**")
            if (propertiesReceiver.dependsOn.isNotEmpty()) propertiesReceiver.dependsOn.forEach { dependsOn(it) }
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
}

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
        val lib = target.libs.findLibrary("detektFormatting").get().get()
        target.dependencies.add("detektPlugins", "${lib.group}:${lib.name}:${lib.version}")
    }
}
