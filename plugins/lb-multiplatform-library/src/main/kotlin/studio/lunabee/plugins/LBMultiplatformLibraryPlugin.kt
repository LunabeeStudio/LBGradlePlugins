@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import javax.inject.Inject

/**
 * This extension allows you to fully configure a Multiplatform library module.
 * It sets up some default information. Refer to the [PropertiesReceiver] class for more details.
 */
open class LBMultiplatformLibraryExtension @Inject constructor(private val project: Project) {
    private val propertiesReceiver = PropertiesReceiver()

    /**
     * @param jvmTarget The target JVM version to use. Default is [JvmTarget.JVM_17].
     * @param targets List of targets you want to enable for your Multiplatform module.
     */
    data class PropertiesReceiver(
        var jvmTarget: JvmTarget = JvmTarget.JVM_17,
        var targets: List<TargetPlatform> = emptyList(),
    )

    fun multiplatform(block: PropertiesReceiver.() -> Unit) {
        block(propertiesReceiver)
        init()
    }

    private fun init() {
        project.kotlinExtension.compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
        project.kotlinExtension.setTargets()
    }

    private fun KotlinMultiplatformExtension.setTargets() {
        propertiesReceiver.targets.forEach { targetPlatform ->
            when (targetPlatform) {
                is TargetPlatform.Ios -> {
                    listOf(
                        iosX64(),
                        iosArm64(),
                        iosSimulatorArm64(),
                    ).forEach {
                        it.binaries.framework {
                            targetPlatform.frameworkConfig(this)
                        }
                    }
                }

                is TargetPlatform.Android -> androidTarget {
                    compilerOptions.jvmTarget.set(propertiesReceiver.jvmTarget)
                    targetPlatform.configure(this)
                }

                is TargetPlatform.Jvm -> jvm {
                    compilerOptions.jvmTarget.set(propertiesReceiver.jvmTarget)
                    targetPlatform.configure(this)
                }
            }
        }
    }
}

/**
 * Usage:
 * ```
 * // Top-level build file where you can add configuration options common to all sub-projects/modules.
 * plugins {
 *     alias(libs.plugins.lbMultiplatformLibrary).apply(false)
 * }
 *
 * // In app `build.gradle.kts`:
 * import org.jetbrains.kotlin.gradle.dsl.JvmTarget
 * import studio.lunabee.plugins.TargetPlatform
 *
 * plugins {
 *     alias(libs.plugins.lbMultiplatformLibrary)
 *     alias(libs.plugins.lbAndroidLibrary) // mandatory if TargetPlatform.Android is added
 * }
 *
 * lbAndroidLibrary {
 *     android {
 *         namespace = "my.application.namespace"
 *         withCompose = false
 *     }
 * }
 *
 * lbMultiplatformLibrary {
 *     multiplatform {
 *         jvmTarget = JvmTarget.JVM_17
 *         targets = listOf(
 *             TargetPlatform.Android(),
 *             TargetPlatform.Jvm(),
 *             TargetPlatform.Ios {
 *                 // Optional configuration for iOS (export...)
 *             },
 *         )
 *     }
 * }
 *
 * kotlin {
 *     sourceSets {
 *         androidMain.dependencies {
 *             // Android dependencies goes here.
 *         }
 *         commonMain.dependencies {
 *             // Common dependencies goes here.
 *         }
 *         iosMain.dependencies {
 *             // iOS dependencies goes here.
 *         }
 *         jvmMain.dependencies {
 *             // Jvm dependencies goes here.
 *         }
 *     }
 * }
 * ```
 */
class LBMultiplatformLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create(
            "lbMultiplatformLibrary",
            LBMultiplatformLibraryExtension::class.java,
        )
        target.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
    }
}

internal val Project.kotlinExtension
    get() = extensions.getByName("kotlin") as KotlinMultiplatformExtension
