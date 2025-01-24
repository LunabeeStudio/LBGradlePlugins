package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import kotlin.system.measureTimeMillis

abstract class SortBuildDependenciesTask : DefaultTask() {

    @get:Input
    abstract val verboseProp: Property<Boolean>

    @Suppress("NestedBlockDepth")
    @TaskAction
    fun run() {
        val verbose = verboseProp.get()
        val sortBuildDependenciesFile = SortBuildDependenciesFile(verbose)

        // Use fileTree to recursively search for all build.gradle.kts files in the project
        val fileTree = project.fileTree(project.rootDir) {
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
