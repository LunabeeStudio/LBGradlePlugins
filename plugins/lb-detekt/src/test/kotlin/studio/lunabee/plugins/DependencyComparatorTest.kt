package studio.lunabee.plugins

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DependencyComparatorTest {

    private val comparator = DependencyComparator(verbose = true)

    @Test
    fun configuration_test() {
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

    @Test
    fun multiline_block_test() {
        val input = listOf(
            "implementation(libs.aaa)",
            "implementation(libs.ccc) {\n    exclude(libs.mmm)\n}",
            "if(condition) {\n    api(libs.vvv)\n} else {\n    api(libs.bbb)\n}",
            "api(uuu)",
        )
        val actual = input.shuffled().sortedWith(comparator).joinToString("\n")

        val expected =
            """
            implementation(libs.aaa)
            implementation(libs.ccc) {
                exclude(libs.mmm)
            }
            api(uuu)
            if(condition) {
                api(libs.vvv)
            } else {
                api(libs.bbb)
            }
            """.trimIndent()

        assertEquals(expected, actual, actual)
    }
}
