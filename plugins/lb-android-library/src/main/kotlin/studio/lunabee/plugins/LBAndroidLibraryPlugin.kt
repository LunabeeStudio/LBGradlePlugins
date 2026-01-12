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
 * Last modified 1/31/25, 8:47 AM
 */

@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This plugin allows you to configure an Android library. Use it as follows:
 * ```
 * plugins {
 *     alias(libs.plugins.lbAndroidLibrary)
 * }
 *
 * lbAndroidLibrary {
 *     android {
 *         namespace = "my.application.namespace"
 *         // Set additional parameters here depending on your needs, see [LBAndroidLibraryExtension.PropertiesReceiver].
 *     }
 * }
 * ```
 */
class LBAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("lbAndroidLibrary", LBAndroidLibraryExtension::class.java)
        target.pluginManager.apply("com.android.library")
    }
}
