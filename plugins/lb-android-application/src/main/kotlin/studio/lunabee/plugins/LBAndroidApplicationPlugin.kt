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
 * Last modified 5/15/25, 3:09 PM
 */

@file:Suppress("unused")

package studio.lunabee.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This plugin allows you to configure your Android application. Generally, you will use it only in your app module as follows:
 * ```
 * plugins {
 *     alias(libs.plugins.lbAndroidApplication)
 * }
 *
 * lbAndroidApplication {
 *     android {
 *         applicationId = "my.application.id"
 *         namespace = "my.application.namespace"
 *         compileSdk = 35
 *         minSdk = 26
 *         versionCode = 102
 *         versionName = "6.0.0"
 *         // Set additional parameters here depending on your needs, see [LBAndroidApplicationExtension.PropertiesReceiver].
 *     }
 * }
 * ```
 */
class LBAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.android.application")
        target.pluginManager.apply("org.jetbrains.kotlin.android")
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        target.extensions.create("lbAndroidApplication", LBAndroidApplicationExtension::class.java)
    }
}
