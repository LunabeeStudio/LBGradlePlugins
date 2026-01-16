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
 * Last modified 9/25/25, 10:11 AM
 */

@file:Suppress("unused")

package studio.lunabee.plugins

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import javax.inject.Inject

/**
 * This extension allows you to fully configure a Multiplatform library module.
 * It sets up some default information. Refer to the [PropertiesReceiver] class for more details.
 */
open class LBMultiplatformAndroidLibraryExtension @Inject constructor(private val project: Project) {
    private val propertiesReceiver = PropertiesReceiver()

    /**
     * @param jvmTarget The target JVM version to use. Default is [JvmTarget.JVM_21].
     * @param targets List of targets you want to enable for your Multiplatform module.
     * @param namespace The namespace for your library. Error will be thrown is not set.
     * @param compileSdk The SDK version used to compile the library. Default is 36.
     * @param withCompose Whether to apply the Compose plugin. Default is false.
     * @param minSdk The minimum SDK version required to run the application. Default is 23.
     * @param jdkVersion The version of the Java Development Kit (JDK) to be used. Default is [JavaVersion.VERSION_21].
     * @param configureAndroidExtension A lambda for additional configuration of the Android application module extension.
     */
    data class PropertiesReceiver(
        var jvmTarget: JvmTarget = JvmTarget.JVM_21,
        var targets: List<TargetPlatform> = emptyList(),
        var namespace: String = "",
        var compileSdk: Int = 36,
        var withCompose: Boolean = false,
        var minSdk: Int = 23,
        var jdkVersion: JavaVersion = JavaVersion.VERSION_21,
        var configureAndroidExtension: KotlinMultiplatformAndroidLibraryTarget.() -> Unit = { },
    )

    fun multiplatform(block: PropertiesReceiver.() -> Unit) {
        block(propertiesReceiver)
        init()
    }

    private fun init() {
        project.extensions.configure<KotlinMultiplatformExtension>("kotlin") {
            compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

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

                    is TargetPlatform.Android -> {
                        extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("androidLibrary") {
                            // Set Android HtmlStyle.parameters from [propertiesReceiver] object.
                            namespace = propertiesReceiver.namespace
                            compileSdk = propertiesReceiver.compileSdk

                            // Set default configuration parameters from [propertiesReceiver] object.
                            minSdk = propertiesReceiver.minSdk

                            compilerOptions.jvmTarget.set(propertiesReceiver.jvmTarget)
                            targetPlatform.configure(this)

                            propertiesReceiver.configureAndroidExtension(this)
                        }
                    }

                    is TargetPlatform.Jvm -> jvm {
                        compilerOptions.jvmTarget.set(propertiesReceiver.jvmTarget)
                        targetPlatform.configure(this)
                    }
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
 * }
 *
 * lbMultiplatformLibrary {
 *     multiplatform {
 *         jvmTarget = JvmTarget.JVM_21
 *         targets = listOf(
 *             TargetPlatform.Android(),
 *             TargetPlatform.Jvm(),
 *             TargetPlatform.Ios {
 *                 // Optional configuration for iOS (export...)
 *             },
 *         )
 *     }
 * }
 * ```
 */
class LBMultiplatformAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("lbMultiplatformLibrary", LBMultiplatformAndroidLibraryExtension::class.java)
        target.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        target.pluginManager.apply("com.android.kotlin.multiplatform.library")
    }
}
