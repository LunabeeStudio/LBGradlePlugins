@file:Suppress("unused")

package studio.lunabee.plugins

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import studio.lunabee.plugins.LBDetektExtension.PropertiesReceiver
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

        // Register sort dependencies script.
        project.registerSortLibs()
        project.registerSortDependencies()

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
            dependsOn("sortLibsVersionsToml", "sortBuildDependencies")
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
        target.dependencies.add("detektPlugins", target.buildDependency("detektFormatting"))
    }
}

@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun Project.registerSortDependencies() {
    tasks.register("sortBuildDependencies") {
        group = "formatting"
        description = "Sorts dependencies and plugins alphabetically in all build.gradle.kts files throughout the project."
        doLast {
            val platformMatcher = "platform("
            val projectMatcher = "project("
            val projectsMatcher = "(projects."
            val kspMatcher = "ksp("
            val testMatcher = "test("
            val comparator = Comparator<String> { o1, o2 ->
                checkNotNull(o1)
                checkNotNull(o2)
                when {
                    platformMatcher in o1 && platformMatcher !in o2 -> -1
                    platformMatcher !in o1 && platformMatcher in o2 -> 1
                    else -> o1.compareTo(o2)
                }
            }
            // Use fileTree to recursively search for all build.gradle.kts files in the project
            fileTree(project.rootDir) {
                include("**/build.gradle.kts") // Include all build.gradle.kts files in all subdirectories
                exclude("buildSrc/**")
            }.forEach { buildFile ->
                // Read the file content
                val lines = buildFile.readLines()
                val sortedLines = mutableListOf<String>()
                var insideTargetBlock = false
                val targetBlocks = listOf(
                    "commonMain.dependencies {",
                    "androidMain.dependencies {",
                    "iosTest.dependencies {",
                    "iosMain.dependencies {",
                    "dependencies {",
                )
                val dependencyLines = mutableListOf<String>()
                lines.forEach { line ->
                    // Check if the line matches one of the target blocks
                    if (targetBlocks.any { block -> line.trim() == block }) {
                        insideTargetBlock = true
                        sortedLines.add(line) // Add the target block line (e.g., 'dependencies {')
                    } else if (insideTargetBlock && line.trim() == "}") {
                        dependencyLines.sortWith(comparator)
                        val groupedLines = dependencyLines.groupBy {
                            when {
                                platformMatcher in it -> 0
                                kspMatcher in it -> 100
                                projectMatcher in it -> 300
                                projectsMatcher in it -> 300
                                testMatcher in it -> 400
                                else -> 200
                            }
                        }.toSortedMap()

                        groupedLines.values.forEach {
                            sortedLines.addAll(it)
                            sortedLines.add("")
                        }

                        sortedLines.removeLast() // Remove the last empty line
                        sortedLines.add(line) // Add the closing '}' line
                        insideTargetBlock = false
                        dependencyLines.clear() // Clear the list for the next target block
                    } else if (insideTargetBlock) {
                        // Collect all lines inside the target block. Filter blank lines and dup
                        if (line.isNotBlank() && !dependencyLines.contains(line)) {
                            dependencyLines.add(line)
                        }
                    } else {
                        // Add all other lines outside the target blocks
                        sortedLines.add(line)
                    }
                }
                // Ensure an empty line at the end of the file
                if (sortedLines.isNotEmpty() && sortedLines.last().isNotBlank()) {
                    sortedLines.add("") // Add an empty line at the end if not already present
                }
                // Write the sorted content back to the file
                buildFile.writeText(sortedLines.joinToString("\n"))
            }
        }
    }
}

@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun Project.registerSortLibs() {
    tasks.register("sortLibsVersionsToml") {
        group = "formatting"
        description = "Sorts the libraries and versions in libs.versions.toml alphabetically"

        doLast {
            val tomlFile = file("gradle/libs.versions.toml")
            val lines = tomlFile.readLines()
            val sortedLines = mutableListOf<String>()
            var insideSection = false
            val sectionLines = mutableListOf<Pair<String?, String>>() // Pair of (comment, line)
            var currentComment: String? = null
            lines.forEach { line ->
                // Detect the start of a new section (e.g., [versions] or [libraries])
                if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                    // Sort and add the previous section, if any
                    if (insideSection) {
                        sectionLines.sortBy { it.second } // Sort by the actual dependency line
                        sectionLines.forEach { (comment, content) ->
                            if (comment != null) sortedLines.add(comment) // Add the comment line, if any
                            sortedLines.add(content) // Add the actual dependency line
                        }
                        sectionLines.clear() // Clear the section lines for the next section
                    }
                    // Start of a new section
                    insideSection = true
                    // Ensure an empty line before each new section
                    sortedLines.add("") // Add an empty line before the section header
                    sortedLines.add(line) // Add the section header (e.g., [libraries])
                    sortedLines.add("") // Add an empty line after the section header
                } else if (insideSection) {
                    if (line.trim().startsWith("#")) {
                        // If we encounter a comment, store it temporarily
                        currentComment = line
                    } else if (line.trim().isNotEmpty()) {
                        // If it's a non-empty dependency line, pair it with the comment (if any) and add to sectionLines
                        sectionLines.add(currentComment to line)
                        currentComment = null // Reset the comment after it's paired
                    }
                } else {
                    // Handle lines outside of sections, ensuring empty lines are preserved
                    if (line.isNotBlank() || (sortedLines.isNotEmpty() && sortedLines.last().isNotBlank())) {
                        sortedLines.add(line) // Add non-section lines (like empty lines or other content)
                    }
                }
            }
            // Sort and add the last section, if needed
            if (insideSection) {
                sectionLines.sortBy { it.second } // Sort by the actual dependency line
                sectionLines.forEach { (comment, content) ->
                    if (comment != null) sortedLines.add(comment)
                    sortedLines.add(content)
                }
            }
            // Ensure an empty line at the end of the file
            if (sortedLines.isNotEmpty() && sortedLines.last().isNotBlank()) {
                sortedLines.add("") // Add an empty line at the end if not already present
            }
            // Write the sorted content back to the file
            tomlFile.writeText(sortedLines.joinToString("\n"))
        }
    }
}
