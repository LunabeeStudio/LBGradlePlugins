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

import org.gradle.api.GradleException
import org.gradle.process.ExecOperations
import java.io.File

internal object DownloadStringsScript {
    const val ResourceName: String = "downloadStrings.sh"
    private const val PluralsScriptResourceName: String = "duplicate_plural_forms.py"
    private const val DeduplicateScriptResourceName: String = "deduplicate_strings.py"

    fun extract(destFolder: File): File {
        // The python helpers are invoked by downloadStrings.sh from its own directory.
        extractResource(PluralsScriptResourceName, destFolder)
        extractResource(DeduplicateScriptResourceName, destFolder)
        return extractResource(ResourceName, destFolder)
    }

    private fun extractResource(resourceName: String, destFolder: File): File {
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

    fun run(
        eo: ExecOperations,
        script: File,
        apiKey: String,
        stringsPath: File,
        stringsFilename: String,
        replaceApostrophes: Boolean,
        replaceQuotes: Boolean,
    ) {
        eo.exec {
            commandLine(
                script.absolutePath,
                apiKey,
                stringsPath,
                stringsFilename,
                replaceApostrophes.toString(),
                replaceQuotes.toString(),
            )
        }
    }
}
