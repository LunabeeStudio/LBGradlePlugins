package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class SortBuildDependenciesTask : DefaultTask() {

    private val platformMatcher = "platform("
    private val projectMatcher = "project("
    private val projectsMatcher = "(projects."
    private val kspMatcher = "ksp("
    private val testMatcher = "test("

    private val comparator = DependencyComparator(platformMatcher)

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

private class DependencyComparator(val platformMatcher: String) : Comparator<String> {
    override fun compare(p0: String?, p1: String?): Int {
        checkNotNull(p0)
        checkNotNull(p1)

        return when {
            platformMatcher in p0 && platformMatcher !in p1 -> -1
            platformMatcher !in p0 && platformMatcher in p1 -> 1
            else -> p0.compareTo(p1)
        }
    }
}
