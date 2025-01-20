package studio.lunabee.plugins

import kotlin.test.Test
import kotlin.test.assertContentEquals

class SortBuildDependenciesFileTest {

    private val sortBuildDependenciesFile = SortBuildDependenciesFile()

    @Test
    fun matcher_blocks_test() {
        val input = """
            dependencies {
                implementation(project("aaa"))
                implementation(platform("aaa"))
                ksp("aaa")
                test("aaa")
                implementation(projects.aaa)
            }
        """.trimIndent().split("\n")
        val actual = sortBuildDependenciesFile.sortLines(input)

        val expected = """
            dependencies {
                implementation(platform("aaa"))

                ksp("aaa")

                implementation(projects.aaa)
                implementation(project("aaa"))

                test("aaa")
            }
        """.trimIndent().split("\n")

        assertContentEquals(expected, actual, actual.joinToString("\n"))
    }
}
