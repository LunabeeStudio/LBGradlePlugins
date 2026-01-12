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

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class SortLibsVersionsTomlTask : DefaultTask() {

    @get:Inject
    abstract val projectLayout: ProjectLayout

    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    @TaskAction
    fun run() {
        val file = projectLayout.projectDirectory.file("gradle/libs.versions.toml").asFile
        if (!file.exists()) return
        val lines = file.readLines()
        val sortedLines = mutableListOf<String>()
        var insideSection = false
        val sectionLines = mutableListOf<Pair<String?, String>>() // Pair of (comment, line)
        var currentComment: String? = null
        lines.forEach { line ->
            // Detect the start of a new section (e.g., [versions] or [libraries])
            if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                // Sort and add the previous section, if any
                if (insideSection) {
                    sectionLines.sortBy { it.second } // Sort by the actual dependency line
                    sectionLines.forEach { (comment, content) ->
                        if (comment != null) sortedLines.add(comment) // Add the comment line, if any
                        sortedLines.add(content) // Add the actual dependency line
                    }
                    sectionLines.clear() // Clear the section lines for the next section
                }
                // Start of a new section
                insideSection = true
                // Ensure an empty line before each new section
                sortedLines.add("") // Add an empty line before the section header
                sortedLines.add(line) // Add the section header (e.g., [libraries])
                sortedLines.add("") // Add an empty line after the section header
            } else if (insideSection) {
                if (line.trim().startsWith("#")) {
                    // If we encounter a comment, store it temporarily
                    currentComment = line
                } else if (line.trim().isNotEmpty()) {
                    // If it's a non-empty dependency line, pair it with the comment (if any) and add to sectionLines
                    sectionLines.add(currentComment to line)
                    currentComment = null // Reset the comment after it's paired
                }
            } else {
                // Handle lines outside of sections, ensuring empty lines are preserved
                if (line.isNotBlank() || (sortedLines.isNotEmpty() && sortedLines.last().isNotBlank())) {
                    sortedLines.add(line) // Add non-section lines (like empty lines or other content)
                }
            }
        }
        // Sort and add the last section, if needed
        if (insideSection) {
            sectionLines.sortBy { it.second } // Sort by the actual dependency line
            sectionLines.forEach { (comment, content) ->
                if (comment != null) sortedLines.add(comment)
                sortedLines.add(content)
            }
        }
        // Ensure an empty line at the end of the file
        if (sortedLines.isNotEmpty() && sortedLines.last().isNotBlank()) {
            sortedLines.add("") // Add an empty line at the end if not already present
        }
        // Write the sorted content back to the file
        file.writeText(sortedLines.joinToString("\n"))
    }
}
