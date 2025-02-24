package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import kotlin.system.measureTimeMillis

abstract class SortBuildDependenciesTask : DefaultTask() {

    @get:Input
    abstract val verboseProp: Property<Boolean>

    @get:Inject
    abstract val projectLayout: ProjectLayout

    @TaskAction
    fun run() {
        val verbose = verboseProp.get()
        val sortBuildDependenciesFile = SortBuildDependenciesFile(verbose)

        // Use fileTree to recursively search for; all build.gradle.kts files in the project
        val fileTree = projectLayout.projectDirectory.asFileTree.matching {
            include("**/build.gradle.kts") // Include all build.gradle.kts files in all subdirectories
            exclude("buildSrc/**")
        }
        val time = measureTimeMillis {
            fileTree.forEach { buildFile ->
                // Read the file content
                val lines = buildFile.readLines()
                val sortedLines: List<String> = sortBuildDependenciesFile.sortLines(lines)
                // Ensure an empty line at the end of the file
                // Write the sorted content back to the file
                buildFile.writeText(sortedLines.joinToString("\n"))

                if (sortedLines.isNotEmpty() && sortedLines.last().isNotBlank()) {
                    buildFile.appendText("\n")
                }
            }
        }
        if (verbose) {
            println("Sorted ${fileTree.count()} files in $time ms")
        }
    }
}
