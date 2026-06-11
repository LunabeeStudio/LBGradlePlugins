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

    /**
     * Git ref used as the conflict-detection baseline (the last synced state). Empty means
     * auto-resolve the repository's default branch (origin/HEAD, then origin/main, origin/master,
     * main, master). Override to pin a specific branch/ref.
     */
    @get:Input
    abstract val baselineRef: Property<String>

    @get:Inject
    abstract val eo: ExecOperations

    @get:Inject
    abstract val projectLayout: ProjectLayout

    /**
     * One locale to synchronize: the local strings file of a `values*` resource directory plus
     * the per-locale work files used by the diff/upload/restore steps.
     */
    private data class LocaleSync(
        val locale: String,
        val stringsFile: File,
        val snapshotFile: File,
        val uploadFile: File,
        val baseFile: File,
        val conflictsFile: File,
        var hasBase: Boolean = false,
    )

    // The download/upload flow catches every failure to roll the working files back to their snapshot;
    // a broad catch is intentional so no error path can leave locally added strings dropped on disk.
    @Suppress("TooGenericExceptionCaught")
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
        val resDir = File(stringsPath, "res")
        val replaceApostrophesValue = replaceApostrophes.get()
        val replaceQuotesValue = replaceQuotes.get()

        if (!File(resDir, "values/$stringsFilename.xml").exists()) {
            throw GradleException("Expected strings file not found: ${File(resDir, "values/$stringsFilename.xml").absolutePath}")
        }

        // Every locale present locally is synchronized: the default `values` dir maps to the
        // provider's source locale (en), `values-XX` dirs map to their translation locale.
        val locales = (resDir.listFiles().orEmpty())
            .filter { it.isDirectory && (it.name == "values" || it.name.startsWith("values-")) }
            .filter { File(it, "$stringsFilename.xml").exists() }
            .sortedBy { it.name }
            .map { dir ->
                val locale = localeOf(dir.name)
                LocaleSync(
                    locale = locale,
                    stringsFile = File(dir, "$stringsFilename.xml"),
                    snapshotFile = File(workDir, "$stringsFilename-${dir.name}-snapshot.xml"),
                    uploadFile = File(workDir, "$stringsFilename-${dir.name}-to-upload.xml"),
                    baseFile = File(workDir, "$stringsFilename-${dir.name}-base.xml"),
                    conflictsFile = File(workDir, "$stringsFilename-${dir.name}-conflicts.tsv"),
                )
            }

        // Baseline = the strings file on the default branch (e.g. origin/master) = last synced state.
        // Enables modification sync + conflict detection. A local modification is detected as long as it
        // is not yet merged into that branch, so devs can freely commit on their feature branch.
        locales.forEach { entry ->
            entry.hasBase = extractGitBase(entry.stringsFile, projectDirectory, entry.baseFile)
            entry.stringsFile.copyTo(entry.snapshotFile, overwrite = true)
        }

        // From here on the working files are overwritten by the download. If anything fails (a failed
        // download, an upload error), roll every working file back to its pre-run snapshot so locally
        // added strings — which don't exist remotely yet and are dropped by the download — are never
        // lost. The per-locale work files are always cleaned up.
        try {
            runDownload(downloadScript, apiKey, stringsPath, stringsFilename, replaceApostrophesValue, replaceQuotesValue)

            locales.forEach { entry ->
                eo.exec {
                    commandLine(
                        buildList {
                            add("python3")
                            add(synchronizeScript.absolutePath)
                            add("sync")
                            add("--api-key")
                            add(apiKey)
                            add("--locale")
                            add(entry.locale)
                            add("--before")
                            add(entry.snapshotFile.absolutePath)
                            add("--after")
                            add(entry.stringsFile.absolutePath)
                            add("--output")
                            add(entry.uploadFile.absolutePath)
                            add("--conflicts-output")
                            add(entry.conflictsFile.absolutePath)
                            if (entry.hasBase) {
                                add("--base")
                                add(entry.baseFile.absolutePath)
                            }
                        },
                    )
                }
            }

            if (locales.any { hasUploadedResources(it.uploadFile) }) {
                runDownload(downloadScript, apiKey, stringsPath, stringsFilename, replaceApostrophesValue, replaceQuotesValue)
            }

            // Restore the dev's local value for any conflicting key so it survives the download(s) that
            // overwrote the working file with the remote value. Runs last, after the final re-download.
            locales.forEach { entry ->
                if (hasConflicts(entry.conflictsFile)) {
                    eo.exec {
                        commandLine(
                            "python3",
                            synchronizeScript.absolutePath,
                            "restore",
                            "--target", entry.stringsFile.absolutePath,
                            "--source", entry.snapshotFile.absolutePath,
                            "--conflicts", entry.conflictsFile.absolutePath,
                        )
                    }
                }
            }
        } catch (error: Exception) {
            // A failed download/upload may have left the working files holding remote content with the
            // locally added strings dropped. Restore each locale from its pre-run snapshot, then fail.
            locales.forEach { entry ->
                if (entry.snapshotFile.exists()) {
                    entry.snapshotFile.copyTo(entry.stringsFile, overwrite = true)
                }
            }
            throw error
        } finally {
            locales.forEach { entry ->
                entry.snapshotFile.delete()
                entry.uploadFile.delete()
                entry.baseFile.delete()
                entry.conflictsFile.delete()
            }
        }
    }

    /**
     * Map an Android `values*` resource directory name to its Loco locale code:
     * `values` → `en` (source locale), `values-fr` → `fr`, `values-zh-rTW` → `zh-TW`,
     * `values-b+zh+Hans` → `zh-Hans`.
     */
    private fun localeOf(dirName: String): String = when {
        dirName == "values" -> "en"
        dirName.startsWith("values-b+") -> dirName.removePrefix("values-b+").replace('+', '-')
        else -> dirName.removePrefix("values-").replace("-r", "-")
    }

    /**
     * Write the version of [stringsFile] on the baseline branch to [baseFile]. Returns false
     * (modification sync disabled) when the file is untracked, the baseline ref is unavailable,
     * or git is missing.
     */
    private fun extractGitBase(stringsFile: File, projectDir: File, baseFile: File): Boolean {
        val relPath = gitOutput(projectDir, listOf("ls-files", "--full-name", "--", stringsFile.absolutePath))
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val ref = resolveBaselineRef(projectDir)
        if (relPath == null || ref == null) return false

        return runCatching {
            val stdout = ByteArrayOutputStream()
            val result = eo.exec {
                workingDir = projectDir
                commandLine("git", "show", "$ref:$relPath")
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

    /**
     * Resolve the git ref used as baseline: the configured [baselineRef] if set, otherwise the
     * repository's default branch (origin/HEAD), falling back to the first existing of
     * origin/main, origin/master, main, master.
     */
    private fun resolveBaselineRef(projectDir: File): String? {
        return baselineRef.get().trim().takeIf { it.isNotEmpty() }
            ?: gitOutput(projectDir, listOf("rev-parse", "--abbrev-ref", "origin/HEAD"))
                ?.trim()
                ?.takeIf { it.isNotEmpty() && it != "origin/HEAD" }
            ?: listOf("origin/main", "origin/master", "main", "master")
                .firstOrNull { refExists(projectDir, it) }
    }

    private fun refExists(projectDir: File, ref: String): Boolean =
        gitOutput(projectDir, listOf("rev-parse", "--verify", "--quiet", "$ref^{commit}")) != null

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

    private fun hasConflicts(conflictsFile: File): Boolean =
        conflictsFile.exists() && conflictsFile.readText().isNotBlank()

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
