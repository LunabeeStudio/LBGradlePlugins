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

        val lineIterator = lines.toMutableList().listIterator()
        while (lineIterator.hasNext()) {
            val line = lineIterator.next()
            // Check if the line matches one of the target blocks
            if (targetBlocks.any { block -> block.matches(line.trim()) }) {
                insideTargetBlock = true
                sortedLines.add(line) // Add the target block line (e.g., 'dependencies {')
            } else if (insideTargetBlock && line.trim() == "}") {
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
                    val sorted = it.sortedWith(comparator)
                    sortedLines.addAll(sorted)
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

                        log("Add dependency line: $builder")
                        dependencyLines.add(builder.toString())
                    } else if (line.trimStart().startsWith("//")) {
                        // Handle comments by aggregating them in one line entry and let the algorithm continue
                        if (lineIterator.hasNext()) {
                            var aggregatedLine = line
                            var nextLine = lineIterator.next()

                            val aggregate = {
                                lineIterator.remove()
                                lineIterator.previous()
                                aggregatedLine += "\n$nextLine"
                                lineIterator.set(aggregatedLine)
                            }

                            while (nextLine.trimStart().startsWith("//") && lineIterator.hasNext() && nextLine.trim() != "}") {
                                aggregate()
                                lineIterator.next()
                                nextLine = lineIterator.next()
                            }

                            if (nextLine.endsWith("{")) {
                                // Entering block, aggregate comment and bloc start line and continue to handle it as a block
                                aggregate()
                                lineIterator.previous() // Back to aggregated comments + block start line
                            } else if (nextLine.trim() == "}") {
                                // End of deps block, add the the current aggregation as it and continue to handle the end of deps block
                                log("Add dependency line: $aggregatedLine")
                                dependencyLines.add(aggregatedLine)
                                lineIterator.previous() // Back to last '}'
                            } else {
                                aggregate()
                                log("Add dependency line: $aggregatedLine")
                                dependencyLines.add(aggregatedLine)
                            }
                        }
                    } else {
                        log("Add dependency line: $builder")
                        dependencyLines.add(builder.toString())
                    }
                }
            } else {
                // Add all other lines outside the target blocks
                sortedLines.add(line)
            }
        }
        return sortedLines
    }

    private fun log(text: String) {
        if (verbose) println(text.replace("\n", "\\n"))
    }
}
