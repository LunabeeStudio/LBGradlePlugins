package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import kotlin.system.measureTimeMillis

open class SortBuildDependenciesTask : DefaultTask() {

    private val sortBuildDependenciesFile = SortBuildDependenciesFile()

    @Suppress("NestedBlockDepth")
    @TaskAction
    fun run() {
        // Use fileTree to recursively search for all build.gradle.kts files in the project
        project.fileTree(project.rootDir) {
            include("**/build.gradle.kts") // Include all build.gradle.kts files in all subdirectories
            exclude("buildSrc/**")
        }.forEach { buildFile ->
            // Read the file content
            val lines = buildFile.readLines()
            val sortedLines: List<String>
            val time = measureTimeMillis {
                sortedLines = sortBuildDependenciesFile.sortLines(lines)
            }
            println("Sorted file ${buildFile.name} in $time ms")
            // Ensure an empty line at the end of the file
            // Write the sorted content back to the file
            buildFile.writeText(sortedLines.joinToString("\n"))

            if (sortedLines.isNotEmpty() && sortedLines.last().isNotBlank()) {
                buildFile.appendText("\n")
            }
        }
    }
}
