package studio.lunabee.plugins

import org.jetbrains.dokka.gradle.DokkaExtension

/**
 * Extension for the Lunabee's Dokka plugin
 *
 * @property enableHtmlDoc Enable html output. Default true
 * @property enableModuleReadme Enable module's README.md import. Default true
 */
class LBDokkaExtension(
    private val dokkaExtension: DokkaExtension,
) {
    var docProject: String = "aa"
    var enableHtmlDoc: Boolean = true
    var enableModuleReadme: Boolean = true

    /**
     * Configure Dokka plugin directly
     *
     * @see DokkaExtension
     */
    fun dokka(configure: DokkaExtension.() -> Unit) {
        dokkaExtension.configure()
    }
}
