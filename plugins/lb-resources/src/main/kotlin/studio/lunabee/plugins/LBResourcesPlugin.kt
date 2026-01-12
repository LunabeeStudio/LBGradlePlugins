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

@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class LBResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("lbResources", LBResourcesExtension::class.java)
        val task = target.tasks.register("downloadStrings", DownloadStringsTask::class.java) {
            group = "Lunabee"
            description = "Download all the strings from Loco."
        }
        task.configure {
            when (val provider = extension.provider) {
                is StringsProvider.Loco -> providerApiKey.set(provider.key)
                null -> throw IllegalArgumentException("provider must be set")
            }
            projectDir.set(extension.targetDirectory)
            replaceQuotes.set(extension.replaceQuotes)
            replaceApostrophes.set(extension.replaceApostrophes)
        }
    }
}
