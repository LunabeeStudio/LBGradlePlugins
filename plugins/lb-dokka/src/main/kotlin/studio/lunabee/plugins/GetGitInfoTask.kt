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
 * Last modified 1/31/25, 8:48 AM
 */

package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Extracts Git branch and repository information and writes it to a file.
 * Requires git binary accessible and Git remote named "origin"
 *
 * @property branchFileProvider The output file containing the branch (line 0) and repository name (line 1)
 */
abstract class GetGitInfoTask @Inject constructor(
    project: Project,
    private val execOperations: ExecOperations,
) : DefaultTask() {

    @get:OutputFile
    val branchFileProvider: Provider<RegularFile> = project.layout.buildDirectory.file("git/info")

    @TaskAction
    fun printInfo() {
        val infoFile = branchFileProvider.get().asFile
        infoFile.delete()
        infoFile.parentFile.mkdirs()
        infoFile.createNewFile()

        val branchStdOut = ByteArrayOutputStream()
        execOperations.exec {
            standardOutput = branchStdOut
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        }

        val urlStdOut = ByteArrayOutputStream()
        execOperations.exec {
            standardOutput = urlStdOut
            commandLine("git", "remote", "get-url", "origin")
        }

        val rootDir = ByteArrayOutputStream()
        execOperations.exec {
            standardOutput = rootDir
            commandLine("git", "rev-parse", "--show-toplevel")
        }

        infoFile.appendText(branchStdOut.toString(Charsets.UTF_8).lines().first())
        infoFile.appendText("\n")
        val repoUrl = urlStdOut.toString(Charsets.UTF_8)
        val startRepositoryNameIndex = repoUrl.indexOfAny(charArrayOf(':', '/')) + 1
        infoFile.appendText(repoUrl.substring(startRepositoryNameIndex).substringBefore('.'))
        infoFile.appendText("\n")
        infoFile.appendText(rootDir.toString(Charsets.UTF_8).lines().first())
    }
}
