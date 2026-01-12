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
 * Last modified 3/4/25, 10:50 AM
 */

package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import kotlin.system.measureTimeMillis

abstract class SortBuildDependenciesTask : DefaultTask() {

    @get:Input
    abstract val verboseProp: Property<Boolean>

    @get:Inject
    abstract val projectLayout: ProjectLayout

    @TaskAction
    fun run() {
        val verbose = verboseProp.get()
        val sortBuildDependenciesFile = SortBuildDependenciesFile(verbose)

        // Use fileTree to recursively search for; all build.gradle.kts files in the project
        val fileTree = projectLayout.projectDirectory.asFileTree.matching {
            include("**/build.gradle.kts") // Include all build.gradle.kts files in all subdirectories
            exclude("buildSrc/**")
        }
        val time = measureTimeMillis {
            fileTree.forEach { buildFile ->
                // Read the file content
                val lines = buildFile.readLines()
                val sortedLines: List<String> = sortBuildDependenciesFile.sortLines(lines)
                // Ensure an empty line at the end of the file
                // Write the sorted content back to the file
                buildFile.writeText(sortedLines.joinToString("\n"))

                if (sortedLines.isNotEmpty() && sortedLines.last().isNotBlank()) {
                    buildFile.appendText("\n")
                }
            }
        }
        if (verbose) {
            println("Sorted ${fileTree.count()} files in $time ms")
        }
    }
}
