@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import studio.lunabee.plugins.LBResourcesExtension.PropertiesReceiver
import javax.inject.Inject

/**
 * This extension allows you to fully configure your Android application.
 * It sets up some default information. Refer to the [PropertiesReceiver] class for more details.
 */
open class LBResourcesExtension @Inject constructor(private val project: Project) {
    private val propertiesReceiver: PropertiesReceiver = PropertiesReceiver()

    /**
     * @param targetDirectory directory where your `src` target directory can be found. Must not be null if configured..
     * @param providerKey your provider key that will be used to contact the provider API. Must not be null if configured.
     * @param provider provider name (currently, we only handle [Provider.Loco].
     */
    data class PropertiesReceiver(
        var targetDirectory: String? = null,
        var providerKey: String? = null,
        var provider: Provider = Provider.Loco,
    )

    enum class Provider {
        Loco,
    }

    /**
     * Register a task to download all the strings from your provider.
     * For configuration, see [PropertiesReceiver].
     */
    fun strings(block: PropertiesReceiver.() -> Unit) {
        block(propertiesReceiver)
        init()
    }

    private fun init() {
        project.tasks.register("downloadStrings", DownloadStringsTask::class.java) {
            group = "https://lunabee.studio"
            description = "Download all the strings from Loco."
            providerApiKey.set(propertiesReceiver.providerKey ?: throw IllegalArgumentException("providerKey must be set"))
            projectDir.set(propertiesReceiver.targetDirectory ?: throw IllegalArgumentException("targetDirectory must be set"))
        }
    }
}

class LBResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("lbResources", LBResourcesExtension::class.java)
    }
}
