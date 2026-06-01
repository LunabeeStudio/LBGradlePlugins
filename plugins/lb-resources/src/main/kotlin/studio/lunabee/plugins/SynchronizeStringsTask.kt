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
 */

package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

abstract class SynchronizeStringsTask : DefaultTask() {
    @get:Input
    abstract val providerApiKey: Property<String>

    @get:Input
    abstract val replaceQuotes: Property<Boolean>

    @get:Input
    abstract val replaceApostrophes: Property<Boolean>

    @get:Input
    abstract val projectDir: Property<File>

    @get:Inject
    abstract val eo: ExecOperations

    @get:Inject
    abstract val projectLayout: ProjectLayout

    @TaskAction
    fun synchronizeStrings() {
        ensurePython3Available()

        val workDir = File(projectLayout.buildDirectory.asFile.get(), "lbResources")
        val downloadScript = DownloadStringsScript.extract(workDir)
        val synchronizeScript = extractSynchronizeScript(workDir)

        val projectDirectory = projectDir.get()
        val apiKey = providerApiKey.get()
        val stringsPath = File(projectDirectory, "/src/main/")
        val stringsFilename = "strings"
        val stringsFile = File(stringsPath, "res/values/$stringsFilename.xml")
        val replaceApostrophesValue = replaceApostrophes.get()
        val replaceQuotesValue = replaceQuotes.get()

        if (!stringsFile.exists()) {
            throw GradleException("Expected strings file not found: ${stringsFile.absolutePath}")
        }

        val snapshotFile = File(workDir, "$stringsFilename-snapshot.xml")
        val deletedResourcesFile = File(workDir, "$stringsFilename-to-upload.xml")
        val baseFile = File(workDir, "$stringsFilename-base.xml")

        // Baseline = committed (git HEAD) strings file = last synced state, captured before the download
        // overwrites the working file. Enables modification sync + conflict detection. Local modifications
        // are expected to be uncommitted so that HEAD still represents the baseline.
        val hasBase = extractGitBase(stringsFile, projectDirectory, baseFile)

        stringsFile.copyTo(snapshotFile, overwrite = true)

        runDownload(downloadScript, apiKey, stringsPath, stringsFilename, replaceApostrophesValue, replaceQuotesValue)

        eo.exec {
            commandLine(
                buildList {
                    add("python3")
                    add(synchronizeScript.absolutePath)
                    add("--api-key"); add(apiKey)
                    add("--before"); add(snapshotFile.absolutePath)
                    add("--after"); add(stringsFile.absolutePath)
                    add("--output"); add(deletedResourcesFile.absolutePath)
                    if (hasBase) {
                        add("--base"); add(baseFile.absolutePath)
                    }
                },
            )
        }

        if (hasUploadedResources(deletedResourcesFile)) {
            runDownload(downloadScript, apiKey, stringsPath, stringsFilename, replaceApostrophesValue, replaceQuotesValue)
        }

        snapshotFile.delete()
        deletedResourcesFile.delete()
        baseFile.delete()
    }

    /**
     * Write the git HEAD version of [stringsFile] to [baseFile]. Returns false (modification sync
     * disabled) when the file is untracked, HEAD is unavailable, or git is missing.
     */
    private fun extractGitBase(stringsFile: File, projectDir: File, baseFile: File): Boolean {
        val relPath = gitOutput(projectDir, listOf("ls-files", "--full-name", "--", stringsFile.absolutePath))
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return false

        return runCatching {
            val stdout = ByteArrayOutputStream()
            val result = eo.exec {
                workingDir = projectDir
                commandLine("git", "show", "HEAD:$relPath")
                standardOutput = stdout
                errorOutput = ByteArrayOutputStream()
                isIgnoreExitValue = true
            }
            val bytes = stdout.toByteArray()
            if (result.exitValue != 0 || bytes.isEmpty()) {
                false
            } else {
                baseFile.writeBytes(bytes)
                true
            }
        }.getOrDefault(false)
    }

    private fun gitOutput(dir: File, args: List<String>): String? = runCatching {
        val stdout = ByteArrayOutputStream()
        val result = eo.exec {
            workingDir = dir
            commandLine(listOf("git") + args)
            standardOutput = stdout
            errorOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
        }
        if (result.exitValue == 0) stdout.toString("UTF-8") else null
    }.getOrNull()

    private fun runDownload(
        script: File,
        apiKey: String,
        stringsPath: File,
        stringsFilename: String,
        replaceApostrophes: Boolean,
        replaceQuotes: Boolean,
    ) {
        DownloadStringsScript.run(
            eo = eo,
            script = script,
            apiKey = apiKey,
            stringsPath = stringsPath,
            stringsFilename = stringsFilename,
            replaceApostrophes = replaceApostrophes,
            replaceQuotes = replaceQuotes,
        )
    }

    private fun extractSynchronizeScript(destFolder: File): File {
        val resourceName = "synchronize_new_strings.py"
        val body = this::class.java.classLoader
            .getResource(resourceName)
            ?.readText()
            ?: throw GradleException("Unable to locate bundled script '$resourceName'.")
        if (!destFolder.exists()) destFolder.mkdirs()
        val destFile = File(destFolder, resourceName)
        if (destFile.exists()) destFile.delete()
        destFile.createNewFile()
        destFile.setExecutable(true)
        destFile.writeText(body)
        return destFile
    }

    private fun hasUploadedResources(deletedResourcesFile: File): Boolean =
        deletedResourcesFile.exists() && deletedResourcesFile.readText().contains("name=\"")

    private fun ensurePython3Available() {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = eo.exec {
            commandLine("python3", "--version")
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            throw GradleException(
                "python3 is required to run synchronizeStrings but was not found on PATH. " +
                    "Install Python 3 and ensure 'python3' is available.",
            )
        }
    }
}
