package studio.lunabee.plugins

import kotlin.test.Test
import kotlin.test.assertContentEquals

class DependencyComparatorTest {

    private val comparator = DependencyComparator()

    @Test
    fun api_implementation_test() {
        val expected = listOf(
            "implementation(\"aaa\")",
            "api(\"bbb\")",
            "api(\"ooo\")",
            "implementation(\"ooo\")",
        )
        val actual = expected.shuffled().sortedWith(comparator)

        assertContentEquals(expected, actual)
    }
}