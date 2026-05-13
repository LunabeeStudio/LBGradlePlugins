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

    @get:Inject
    abstract val eo: ExecOperations

    @get:Inject
    abstract val projectLayout: ProjectLayout

    @TaskAction
    fun synchronizeStrings() {
        ensurePython3Available()

        val scriptName = "synchronize_new_strings.py"
        val scriptBody = this::class.java.classLoader
            .getResource(scriptName)
            ?.readText()
            ?: throw GradleException("Unable to locate bundled script '$scriptName'.")

        val destFolder = File("${projectLayout.buildDirectory.asFile.get().absolutePath}/lbResources")
        if (!destFolder.exists()) destFolder.mkdirs()
        val destFile = File(destFolder, scriptName)
        if (destFile.exists()) destFile.delete()
        destFile.createNewFile()
        destFile.setExecutable(true)
        destFile.writeText(scriptBody)

        eo.exec {
            commandLine("python3", destFile.absolutePath, "--api-key", providerApiKey.get())
        }
    }

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
