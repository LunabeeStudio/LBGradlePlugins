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

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import studio.lunabee.plugins.LBAndroidLibraryExtension.PropertiesReceiver
import javax.inject.Inject

/**
 * This extension allows you to fully configure an Android library module.
 * It sets up some default information. Refer to the [PropertiesReceiver] class for more details.
 */
open class LBAndroidLibraryExtension @Inject constructor(private val project: Project) {
    private val propertiesReceiver = PropertiesReceiver()

    /**
     * @param namespace The namespace for your library. Error will be thrown is not set.
     * @param compileSdk The SDK version used to compile the library. Default is 36.
     * @param withCompose Whether to apply the Compose plugin. Default is false.
     * @param enableBuildConfig Whether to enable access to build config field. Default is false.
     * @param minSdk The minimum SDK version required to run the application. Default is 23.
     * @param jdkVersion The version of the Java Development Kit (JDK) to be used. Default is [JavaVersion.VERSION_21].
     * @param configureJava A lambda for additional configuration of the Java plugin extension.
     * @param configureAndroidExtension A lambda for additional configuration of the Android application module extension.
     */
    data class PropertiesReceiver(
        var namespace: String = "",
        var compileSdk: Int = 36,
        var withCompose: Boolean = false,
        var enableBuildConfig: Boolean = false,
        var minSdk: Int = 23,
        var jdkVersion: JavaVersion = JavaVersion.VERSION_21,
        var configureJava: JavaPluginExtension.() -> Unit = { },
        var configureAndroidExtension: LibraryExtension.() -> Unit = { },
    )

    fun android(block: PropertiesReceiver.() -> Unit) {
        block(propertiesReceiver)
        init()
    }

    private fun init() {
        // Apply compose plugin if applicable.
        if (propertiesReceiver.withCompose) {
            project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        }

        // Configure Java Toolchain.
        project.extensions.configure<JavaPluginExtension>("java") {
            toolchain { languageVersion.set(JavaLanguageVersion.of(propertiesReceiver.jdkVersion.toString())) }
            propertiesReceiver.configureJava(this)
        }

        project.extensions.configure<LibraryExtension>("android") {
            // Set Android parameters from [propertiesReceiver] object.
            namespace = propertiesReceiver.namespace
            compileSdk = propertiesReceiver.compileSdk

            // Set default configuration parameters from [propertiesReceiver] object.
            defaultConfig.minSdk = propertiesReceiver.minSdk

            // Set compile options (i.e Java version) from [propertiesReceiver] object.
            compileOptions.sourceCompatibility = propertiesReceiver.jdkVersion
            compileOptions.targetCompatibility = propertiesReceiver.jdkVersion

            // Enable buildConfig feature (to have access to Build variable) and compose (for previews).
            buildFeatures.buildConfig = propertiesReceiver.enableBuildConfig
            if (propertiesReceiver.withCompose) buildFeatures.compose = true

            propertiesReceiver.configureAndroidExtension(this)
        }
    }
}
