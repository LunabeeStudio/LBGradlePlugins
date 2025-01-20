package studio.lunabee.plugins

/**
 * Compare two line of dependencies
 *   • Use the dependency part first (i.e between "..")
 */
class DependencyComparator : Comparator<String> {
    override fun compare(p0: String?, p1: String?): Int {
        checkNotNull(p0)
        checkNotNull(p1)

        val config0 = p0.substringBefore('"')
        val config1 = p1.substringBefore('"')

        val dep0 = p0.substringAfter('"').substringBefore('"')
        val dep1 = p1.substringAfter('"').substringBefore('"')

        val depRes = dep0.compareTo(dep1)
        return if (depRes == 0) {
            // Fallback to configuration if dependencies are equal to avoid non-stable order
            config0.compareTo(config1)
        } else {
            depRes
        }
    }
}