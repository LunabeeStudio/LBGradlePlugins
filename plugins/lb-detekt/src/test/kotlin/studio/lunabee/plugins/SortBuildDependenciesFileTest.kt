package studio.lunabee.plugins

import kotlin.test.Test
import kotlin.test.assertContentEquals

class SortBuildDependenciesFileTest {

    private val sortBuildDependenciesFile = SortBuildDependenciesFile(true)

    @Test
    fun matcher_blocks_test() {
        val input = """
            dependencies {
                implementation(project(aaa))
                implementation(platform(aaa))
                ksp(aaa)
                testImplementation(aaa)
                implementation(libs.aaa)
                implementation(projects.aaa)
            }
        """.trimIndent().split("\n")
        val actual = sortBuildDependenciesFile.sortLines(input)

        val expected = """
            dependencies {
                implementation(platform(aaa))

                ksp(aaa)

                implementation(libs.aaa)

                implementation(project(aaa))
                implementation(projects.aaa)

                testImplementation(aaa)
            }
        """.trimIndent().split("\n")

        assertContentEquals(expected, actual, actual.joinToString("\n"))
    }

    @Test
    fun keep_custom_blocks_test() {
        val expected = """
            dependencies {
                if(condition) {
                    implementation(libs.ddd)
                } else {
                    implementation(libs.ccc)
                }
            }
        """.trimIndent().split("\n")
        val actual = sortBuildDependenciesFile.sortLines(expected).flatMap { it.split("\n") }

        assertContentEquals(expected, actual, actual.joinToString("\n"))
    }

    @Test
    fun sort_custom_blocks_test() {
        val input = """
            dependencies {
                implementation(libs.ooo)
                if(condition) {
                    implementation(libs.ddd)
                } else {
                    implementation(libs.ccc)
                }
                implementation(libs.uuu) {
                    exclude(libs.aaa)
                }
                implementation(libs.aaa)
            }
        """.trimIndent().split("\n")
        val expected = """
            dependencies {
                implementation(libs.aaa)
                if(condition) {
                    implementation(libs.ddd)
                } else {
                    implementation(libs.ccc)
                }
                implementation(libs.ooo)
                implementation(libs.uuu) {
                    exclude(libs.aaa)
                }
            }
        """.trimIndent().split("\n")
        val actual = sortBuildDependenciesFile.sortLines(input).flatMap { it.split("\n") }

        assertContentEquals(expected, actual, actual.joinToString("\n"))
    }
}
