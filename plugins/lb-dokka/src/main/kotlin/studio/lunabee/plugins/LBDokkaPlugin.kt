@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
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
 * lbDokka {
 *     // custom configuration
 * }
 * ```
 */
class LBDokkaPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(DokkaPlugin::class.java)
        val dokkaExtension = target.extensions.findByType(DokkaExtension::class.java)!!

        enableModuleReadme(dokkaExtension, target)
        target.setupHtmlOutput(dokkaExtension)
        target.configureDokkaTask(target)

        // TODO check icon provided

        checkDokkaDeps(target)
    }

    private fun Project.loadResource(targetDir: String, resFile: String) {
        val file = File(layout.buildDirectory.file(targetDir).get().asFile, resFile)
        file.delete()
        file.parentFile.mkdirs()
        file.createNewFile()
        file.outputStream().use { outputStream ->
            this@LBDokkaPlugin::class.java.classLoader.getResource(resFile)!!.openStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /**
     * Includes module root README.md
     */
    private fun enableModuleReadme(dokkaExtension: DokkaExtension, target: Project) {
        dokkaExtension.dokkaSourceSets.configureEach {
            if (File(target.projectDir, "README.md").exists()) {
                includes.from("README.md")
            }
        }
    }

    private fun Project.setupHtmlOutput(dokkaExtension: DokkaExtension) {
        dokkaExtension.pluginsConfiguration.named("html").configure {
            this as DokkaHtmlPluginParameters

            val docsProject = rootProject.project(DocsModule)

            val styles = buildList {
                File(docsProject.projectDir, "styles").listFiles()?.let { addAll(it) }
                docsProject.layout.buildDirectory.file(TmpStyleDir).get().asFile.listFiles()?.let { addAll(it) }
            }
            customStyleSheets.from(styles)

            val images = buildList {
                File(docsProject.projectDir, "images").listFiles()?.let { addAll(it) }
            }
            customAssets.from(images)

            footerMessage.set("&copy; Lunabee Studio")
            homepageLink.set("https://github.com/$HomepagePlaceholder") // cannot be modify after configuration, so use placeholder and replace
        }
    }

    /**
     * Ensure every subproject has the Dokka plugin applied
     */
    private fun checkDokkaDeps(target: Project) {
        target.afterEvaluate {
            val dokkaDeps = target.configurations.firstOrNull { it.name == DOKKA_CONFIGURATION_NAME }?.allDependencies.orEmpty()
            dokkaDeps.forEach {
                val project = target.rootProject.childProjects[it.name]!!
                val hasPlugin = project.pluginManager.hasPlugin("org.jetbrains.dokka")
                if (!hasPlugin) {
                    error("Dokka plugin not found in :${project.name} but added as dokka dependency in :${target.name}")
                }
            }
        }
    }

    /**
     * - Inject Git info to override [HomepagePlaceholder]
     * - Override ui-kit folder assets
     */
    private fun Project.configureDokkaTask(target: Project) {
        val gitInfoTask = target.tasks.register<GetGitInfoTask>("getGitInfoTask")
        target.tasks.withType<DokkaGenerateTask> {
            dependsOn(gitInfoTask)

            doFirst {
                loadResource(TmpStyleDir, "app-styles.css")
                loadResource(TmpUiKitDir, "homepage.svg")
            }

            doLast {
                val docsProject = rootProject.project(DocsModule)

                val gitInfoFileLines = gitInfoTask.get().outputs.files.singleFile.readLines()
                val branch = gitInfoFileLines[0]
                val repositoryName = gitInfoFileLines[1]
                val githubPath = "$repositoryName/tree/$branch/${target.projectDir.relativeTo(target.rootDir).path}"

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

                // https://github.com/Kotlin/dokka/issues/4007
                buildList {
                    File(docsProject.projectDir, "ui-kit").listFiles()?.let { addAll(it) }
                    docsProject.layout.buildDirectory.file(TmpUiKitDir).get().asFile.listFiles()?.let { addAll(it) }
                }.forEach {
                    it.copyTo(outputDirectory.file("ui-kit/assets/${it.name}").get().asFile, overwrite = true)
                }
            }
        }
    }
}
