package studio.lunabee.plugins

/**
 * Compare two line of dependencies
 *   • Use the dependency part first
 *   • Handle multiline dependency with custom blocks (if/else, exclude, etc.)
 */
class DependencyComparator(
    private val catalogName: String = "libs",
    private val verbose: Boolean = false,
) : Comparator<String> {

    private val nonAlphaNumRegex = Regex("[^A-Za-z0-9]")
    private val configMatchers = listOf(
        Regex("\\w*implementation", RegexOption.IGNORE_CASE),
        Regex("\\w*api", RegexOption.IGNORE_CASE),
        Regex("\\w*ksp", RegexOption.IGNORE_CASE),
        Regex("dokka", RegexOption.IGNORE_CASE),
    )

    override fun compare(p0: String?, p1: String?): Int {
        if (verbose) {
            println("Compare [$p0] and [$p1]")
        }

        checkNotNull(p0)
        checkNotNull(p1)

        val config0 = extractConfig(p0)
        val config1 = extractConfig(p1)

        if (verbose) {
            println("Extract config [$config0] and [$config1]")
        }

        val dep0 = extractDependency(p0, config0)
        val dep1 = extractDependency(p1, config1)

        if (verbose) {
            println("Extract dep [$dep0] and [$dep1]")
        }

        val depRes = dep0.compareTo(dep1)
        return if (depRes == 0) {
            // Fallback to configuration if dependencies are equal to avoid non-stable order
            config0.compareTo(config1)
        } else {
            depRes
        }
    }

    private fun extractConfig(line: String): String {
        val config = configMatchers.firstNotNullOf { regex -> regex.find(line) }.value
        return config
    }

    private fun extractDependency(line: String, config: String): String {
        val sub = line
            .substringAfter(config)
            .substringBefore('\n')
            .substringAfterLast('(')
            .substringBefore(')')
            .replace("$catalogName.", "")
        return nonAlphaNumRegex.replace(sub, "")
    }
}
