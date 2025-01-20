package studio.lunabee.plugins

import kotlin.test.Test
import kotlin.test.assertContentEquals

class DependencyComparatorTest {

    private val comparator = DependencyComparator()

    @Test
    fun api_implementation_test() {
        val expected = listOf(
            "implementation(aaa)",
            "api(bbb)",
            "implementation(platform(libs.compose.bom))",
            "androidTestImplementation(libs.datastore.preferences)",
            "api(ooo)",
            "implementation(ooo)",
            "androidTestImplementation(project(\":remote\"))",
        )
        val actual = expected.shuffled().sortedWith(comparator)

        assertContentEquals(expected, actual)
    }
}