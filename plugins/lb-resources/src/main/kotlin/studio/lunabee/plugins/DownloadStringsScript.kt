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

    fun extract(destFolder: File): File {
        val body = this::class.java.classLoader
            .getResource(ResourceName)
            ?.readText()
            ?: throw GradleException("Unable to locate bundled script '$ResourceName'.")
        if (!destFolder.exists()) destFolder.mkdirs()
        val destFile = File(destFolder, ResourceName)
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
