@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaBasePlugin.Companion.DOKKA_CONFIGURATION_NAME
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask
import java.io.File

private const val HomepagePlaceholder: String = "HOMEPAGE_PLACEHOLDER"
private const val TmpUiKitDir = "tmp/lbDokkaUiKit"
private const val TmpStyleDir = "tmp/lbDokkaStyles"
private const val DocsModule = "docs"

/**
 * This plugin initializes and configures the Dokka plugin.
 * ```
 * // Top-level build file where you can add configuration options common to all sub-projects/modules.
 * plugins {
 *     alias(libs.plugins.lbDokka)
 * }
 *
 * dokka {
 *     // Dokka configuration
 * }
 * ```
 */
class LBDokkaPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val pluginMode: Provider<String> = target.providers.gradleProperty("org.jetbrains.dokka.experimental.gradle.pluginMode")
        if (pluginMode.orNull?.startsWith("V2Enabled") != true) {
            error(
                "LBDokka only works with V2 mode. Please add V2 flag to gradle.properties.\n" +
                    "org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled\n" +
                    "org.jetbrains.dokka.experimental.gradle.pluginMode.noWarn=true",
            )
        }

        target.pluginManager.apply(DokkaPlugin::class.java)
        val dokkaExtension = target.extensions.findByType(DokkaExtension::class.java)!!

        enableModuleReadme(target, dokkaExtension)
        setupHtmlOutput(target, dokkaExtension)
        configureDokkaTask(target)
    }

    /**
     * Includes module root README.md
     */
    private fun enableModuleReadme(target: Project, dokkaExtension: DokkaExtension) {
        dokkaExtension.dokkaSourceSets.configureEach {
            if (File(target.projectDir, "README.md").exists()) {
                includes.from("README.md")
            }
        }
    }

    private fun setupHtmlOutput(target: Project, dokkaExtension: DokkaExtension) {
        dokkaExtension.pluginsConfiguration.named("html").configure {
            this as DokkaHtmlPluginParameters

            val docsProject = target.rootProject.project(DocsModule)

            val styles = buildList {
                docsProject.file("styles").listFiles()?.let { addAll(it) }
                docsProject.layout.buildDirectory.file(TmpStyleDir).get().asFile.listFiles()?.let { addAll(it) }
            }
            customStyleSheets.from(styles)

            val images = buildList {
                docsProject.file("images").listFiles()?.let { addAll(it) }
            }
            customAssets.from(images)

            footerMessage.set("&copy; Lunabee Studio")
            homepageLink.set(
                "https://github.com/$HomepagePlaceholder",
            ) // cannot be modify after configuration, so use placeholder and replace
        }
    }

    /**
     * - Inject Git info to override [HomepagePlaceholder]
     * - Override ui-kit folder assets manually
     */
    private fun configureDokkaTask(target: Project) {
        val gitInfoTask = target.tasks.register<GetGitInfoTask>("getGitInfoTask")
        target.tasks.withType<DokkaGenerateTask> {
            dependsOn(gitInfoTask)

            doFirst {
                loadResource(target, TmpStyleDir, "app-styles.css")
                loadResource(target, TmpUiKitDir, "homepage.svg")
                if (target.name == DocsModule) {
                    checkDokkaDeps(target)
                }
                checkAppIcon(target)
            }

            doLast {
                val docsProject = target.rootProject.project(DocsModule)

                val gitInfoFileLines = gitInfoTask.get().outputs.files.singleFile.readLines()
                val branch = gitInfoFileLines[0]
                val repositoryName = gitInfoFileLines[1]
                val rootDir = File(gitInfoFileLines[2])

                val githubPath = "$repositoryName/tree/$branch/${target.projectDir.relativeTo(rootDir).path}"

                outputDirectory.get().asFile.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        val text = file.readText()
                        val updatedText = text.replace(
                            oldValue = HomepagePlaceholder,
                            newValue = githubPath,
                        )
                        if (text != updatedText) {
                            file.writeText(updatedText)
                        }
                    }
                }

                // FIXME https://github.com/Kotlin/dokka/issues/4007
                buildList {
                    docsProject.file("ui-kit").listFiles()?.let { this.addAll(it) }
                    docsProject.layout.buildDirectory.file(TmpUiKitDir).get().asFile.listFiles()?.let { this.addAll(it) }
                }.forEach {
                    it.copyTo(outputDirectory.file("ui-kit/assets/${it.name}").get().asFile, overwrite = true)
                }
            }
        }
    }

    private fun checkAppIcon(target: Project) {
        val docsProject = target.rootProject.project(DocsModule)
        val logoFile = docsProject.file("images/logo-icon.svg")
        if (!logoFile.exists()) {
            target.logger.warn("LBDokka - No icon found at $logoFile")
        }
    }

    /**
     * Ensure every subproject has the Dokka plugin applied
     */
    private fun checkDokkaDeps(doc: Project) {
        val dokkaDeps = doc.configurations.firstOrNull { it.name == DOKKA_CONFIGURATION_NAME }?.allDependencies.orEmpty()
        dokkaDeps.forEach { deps ->
            val project = doc.rootProject.subprojects.first { project -> project.name == deps.name }
            val hasPlugin = project.pluginManager.hasPlugin("org.jetbrains.dokka")
            if (!hasPlugin) {
                error("Dokka plugin not found in :${project.name} but added as dokka dependency in :${doc.name}")
            }
        }
    }

    private fun loadResource(target: Project, targetDir: String, resFile: String) {
        val file = File(target.layout.buildDirectory.file(targetDir).get().asFile, resFile)
        file.delete()
        file.parentFile.mkdirs()
        file.createNewFile()
        file.outputStream().use { outputStream ->
            this::class.java.classLoader.getResource(resFile)!!.openStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
