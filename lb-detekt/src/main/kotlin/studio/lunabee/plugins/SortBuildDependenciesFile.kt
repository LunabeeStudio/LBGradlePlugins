package studio.lunabee.plugins

class SortBuildDependenciesFile {

    private val platformMatcher = "platform("
    private val projectMatcher = "project("
    private val projectsMatcher = "(projects."
    private val kspMatcher = "ksp("
    private val testMatcher = "test("

    private val comparator = DependencyComparator()

    fun sortLines(lines: List<String>): List<String> {
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
        return sortedLines
    }
}
