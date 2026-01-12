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
 * Last modified 12/17/25, 10:14 AM
 */

package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class DownloadStringsTask : DefaultTask() {
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
    fun downloadStrings() {
        val projectDir = projectDir.get()
        val locoApiKey = providerApiKey.get()
        val scriptName = "downloadStrings.sh"
        val configFile = this::class.java.classLoader
            .getResource(scriptName)!!
            .readText()
        val destFolder = File("${projectLayout.buildDirectory.asFile.get().absolutePath}/lbResources")
        if (!destFolder.exists()) destFolder.mkdirs()
        val destFile = File(destFolder, scriptName)
        if (destFile.exists()) destFile.delete()
        destFile.createNewFile()
        destFile.setExecutable(true)
        destFile.writeText(configFile)
        val stringsPath = File(projectDir, "/src/main/")
        val stringsFilename = "strings"
        eo.exec { commandLine(destFile.absolutePath, locoApiKey, stringsPath, stringsFilename, replaceApostrophes, replaceQuotes) }
    }
}
