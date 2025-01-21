package studio.lunabee.plugins

class SortBuildDependenciesFile(
    verbose: Boolean,
) {

    private val platformMatcher = Regex("platform\\(")
    private val projectMatcher = Regex("project\\(")
    private val projectsMatcher = Regex("\\(projects.")
    private val kspMatcher = Regex("ksp\\(")
    private val testMatcher = Regex("test.*\\(")

    private val comparator = DependencyComparator(verbose = verbose)

    @Suppress("NestedBlockDepth")
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

        val lineIterator = lines.iterator()
        while (lineIterator.hasNext()) {
            val line = lineIterator.next()
            // Check if the line matches one of the target blocks
            if (targetBlocks.any { block -> line.trim() == block }) {
                insideTargetBlock = true
                sortedLines.add(line) // Add the target block line (e.g., 'dependencies {')
            } else if (insideTargetBlock && line.trim() == "}") {
                dependencyLines.sortWith(comparator)
                val groupedLines = dependencyLines.groupBy {
                    when {
                        platformMatcher.containsMatchIn(it) -> 0
                        kspMatcher.containsMatchIn(it) -> 100
                        projectMatcher.containsMatchIn(it) -> 300
                        projectsMatcher.containsMatchIn(it) -> 300
                        testMatcher.containsMatchIn(it) -> 400
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
                // Collect all lines inside the target block. Filter blank and duplicated lines
                if (line.isNotBlank() && !dependencyLines.contains(line)) {
                    val builder: StringBuilder = StringBuilder(line)

                    // Handle custom block
                    if (line.endsWith("{")) {
                        var nextLine = lineIterator.next()
                        while (!nextLine.endsWith("}")) {
                            builder.appendLine()
                            builder.append(nextLine)
                            nextLine = lineIterator.next()
                        }
                        builder.appendLine()
                        builder.append(nextLine)
                    }

                    dependencyLines.add(builder.toString())
                }
            } else {
                // Add all other lines outside the target blocks
                sortedLines.add(line)
            }
        }
        return sortedLines
    }
}
