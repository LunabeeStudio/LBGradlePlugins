/*
 * Copyright (c) 2026 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 1/12/2026
 * Last modified 9/25/25, 10:11 AM
 */

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
