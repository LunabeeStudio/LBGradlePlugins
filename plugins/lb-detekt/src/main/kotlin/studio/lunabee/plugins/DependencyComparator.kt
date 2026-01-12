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
 * Last modified 1/24/25, 11:23 AM
 */

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
        Regex("coreLibraryDesugaring", RegexOption.IGNORE_CASE),
    )

    override fun compare(p0: String?, p1: String?): Int {
        log("Compare [${p0?.trim()}] and [${p1?.trim()}]")

        checkNotNull(p0)
        checkNotNull(p1)

        val config0 = extractConfig(p0)
        val config1 = extractConfig(p1)

        log("Extract config [$config0] and [$config1]")

        val dep0 = extractDependency(p0, config0)
        val dep1 = extractDependency(p1, config1)

        log("Extract dep [$dep0] and [$dep1]")

        val depRes = dep0.compareTo(dep1)
        return if (depRes == 0) {
            // Fallback to configuration if dependencies are equal to avoid non-stable order
            config0.compareTo(config1)
        } else {
            depRes
        }
    }

    private fun extractConfig(line: String): String {
        val config = configMatchers.firstNotNullOfOrNull { regex -> regex.find(line) }?.value ?: line.substringBefore('(')
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

    private fun log(text: String) {
        if (verbose) println(text.replace("\n", "\\n"))
    }
}
