package studio.lunabee.plugins

class SortBuildDependenciesFile(
    private val verbose: Boolean,
) {

    private val platformMatcher = Regex("platform\\(")
    private val projectMatcher = Regex("project\\(")
    private val projectsMatcher = Regex("\\(projects.")
    private val kspMatcher = Regex("ksp\\(")
    private val testMatcher = Regex(".*test.*\\(", RegexOption.IGNORE_CASE)
    private val androidTestMatcher = Regex(".*androidTest.*\\(", RegexOption.IGNORE_CASE)
    private val desugaringMatcher = Regex("coreLibraryDesugaring.*\\(")
    private val devConfigMatcher = Regex("dev(Api|Implementation)\\(")
    private val debugConfigMatcher = Regex("debug(Api|Implementation)\\(")
    private val internalConfigMatcher = Regex("internal(Api|Implementation)\\(")

    private val comparator = DependencyComparator(verbose = verbose)

    @Suppress("NestedBlockDepth")
    fun sortLines(lines: List<String>): List<String> {
        val sortedLines = mutableListOf<String>()
        var insideTargetBlock = false
        val targetBlocks = listOf(
            Regex(".*.dependencies \\{"),
            Regex("dependencies \\{"),
        )
        val dependencyLines = mutableListOf<String>()

        val lineIterator = lines.listIterator()
        while (lineIterator.hasNext()) {
            val line = lineIterator.next()
            // Check if the line matches one of the target blocks
            if (targetBlocks.any { block -> block.matches(line.trim()) }) {
                insideTargetBlock = true
                sortedLines.add(line) // Add the target block line (e.g., 'dependencies {')
            } else if (insideTargetBlock && line.trim() == "}") {
                dependencyLines.sortWith(comparator)
                val groupedLines = dependencyLines.groupBy {
                    when {
                        desugaringMatcher.containsMatchIn(it) -> 300
                        platformMatcher.containsMatchIn(it) -> 400
                        kspMatcher.containsMatchIn(it) -> 500
                        androidTestMatcher.containsMatchIn(it) -> 900
                        testMatcher.containsMatchIn(it) -> 1000
                        devConfigMatcher.containsMatchIn(it) -> 800
                        debugConfigMatcher.containsMatchIn(it) -> 810
                        internalConfigMatcher.containsMatchIn(it) -> 820
                        projectMatcher.containsMatchIn(it) -> 700
                        projectsMatcher.containsMatchIn(it) -> 700
                        else -> 600
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

                    if (line.endsWith("{")) { // Handle custom block
                        var nextLine = lineIterator.next()
                        while (!nextLine.endsWith("}") && lineIterator.hasNext()) {
                            builder.appendLine()
                            builder.append(nextLine)
                            nextLine = lineIterator.next()
                        }
                        builder.appendLine()
                        builder.append(nextLine)
                    } else if (line.trimStart().startsWith("//")) { // Handle comments
                        var nextLine = lineIterator.next()
                        while (nextLine.trimStart().startsWith("//") && lineIterator.hasNext() && nextLine.trim() != "}") {
                            builder.appendLine()
                            builder.append(nextLine)
                            nextLine = lineIterator.next()
                        }
                        if (nextLine.trim() == "}") {
                            // Comment is the last line of the target block
                            lineIterator.previous()
                        } else {
                            // Wrap line with comment
                            builder.appendLine()
                            builder.append(nextLine)
                        }
                    }

                    if (verbose) {
                        println("Add dependency line: $builder")
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
