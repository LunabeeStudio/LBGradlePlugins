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
