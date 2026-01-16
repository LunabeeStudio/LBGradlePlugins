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
 * Last modified 8/20/25, 2:55 PM
 */

package studio.lunabee.plugins

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import javax.inject.Inject

private const val DefaultCompileSdk: Int = 36
private const val DefaultMinSdk: Int = 23
private const val DefaultVersionCode: Int = 1
private const val DefaultVersionName: String = "1.0.0"
private val DefaultJdkVersion: JavaVersion = JavaVersion.VERSION_21

/**
 * This extension allows you to fully configure your Android application.
 * It initialize some default information and allows to override them in [android] or [java] blocks which gives access to underlying plugins
 */
open class LBAndroidApplicationExtension @Inject constructor(private val project: Project) {
    init {
        // Configure Java Toolchain.
        project.extensions.configure<JavaPluginExtension>("java") {
            toolchain { languageVersion.set(JavaLanguageVersion.of(DefaultJdkVersion.toString())) }
        }

        // Set default values
        project.extensions.configure<ApplicationExtension>("android") {
            compileSdk = DefaultCompileSdk

            defaultConfig.versionCode = DefaultVersionCode
            defaultConfig.versionName = DefaultVersionName
            defaultConfig.targetSdk = compileSdk
            defaultConfig.minSdk = DefaultMinSdk

            compileOptions.sourceCompatibility = DefaultJdkVersion
            compileOptions.targetCompatibility = DefaultJdkVersion

            // Enable buildConfig feature (to have access to Build variable) and compose (for previews).
            buildFeatures.buildConfig = true
            buildFeatures.compose = true

            // Configure signing config for debug
            signingConfigs {
                maybeCreate("debug").apply {
                    storeFile = project.file("debug.keystore")
                    storePassword = "androiddebug"
                    keyAlias = "debug"
                    keyPassword = "androiddebug"
                }
            }

            // Configure lint option.
            lint {
                disable.add("ObsoleteLintCustomCheck")
                htmlOutput = project.rootProject.file("${project.rootProject.projectDir}/build/reports/lint/lint-report.html")
                xmlOutput = project.rootProject.file("${project.rootProject.projectDir}/build/reports/lint/lint-report.xml")
                textOutput = project.rootProject.file("${project.rootProject.projectDir}/build/reports/lint/lint-report.txt")
            }

            // Configuration from release and debug.
            buildTypes {
                debug {
                    isMinifyEnabled = false
                    signingConfig = signingConfigs.getByName("debug")
                }
                release {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
            }

            packaging {
                resources {
                    pickFirsts += "/META-INF/{AL2.0,LGPL2.1}"
                    pickFirsts += "/META-INF/LICENSE.md"
                    pickFirsts += "/META-INF/LICENSE-notice.md"
                }
            }
        }
    }

    fun android(block: ApplicationExtension.() -> Unit) {
        project.extensions.configure<ApplicationExtension>("android") {
            block()
        }
    }

    fun java(block: JavaPluginExtension.() -> Unit) {
        project.extensions.configure<JavaPluginExtension>("java") {
            block()
        }
    }
}
