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
 * Last modified 6/6/25, 4:05 PM
 */

@file:Suppress("unused")

package studio.lunabee.plugins

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This plugin allows you to configure your Android flavors. Generally, you will use it only in your app module as follows:
 * ```
 * plugins {
 *     alias(libs.plugins.lbAndroidFlavors)
 * }
 * ```
 */
class LBAndroidFlavorsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.withPlugin("com.android.application") {
            setupFlavors(target)
        }
        target.pluginManager.withPlugin("com.android.library") {
            setupFlavors(target)
        }
    }

    private fun setupFlavors(target: Project) {
        target.extensions.configure<ApplicationExtension>("android") {
            // Configure flavors for the app: dev, prod and client, internal. By default, devInternal will be used.
            flavorDimensions += LBFlavorDimension.Environment.Key
            flavorDimensions += LBFlavorDimension.App.Key
            productFlavors {
                create(LBFlavorDimension.Environment.Dev) {
                    dimension = LBFlavorDimension.Environment.Key
                    applicationIdSuffix = ".dev"
                    isDefault = true
                    buildConfigField("Boolean", "IS_DEV", "true")
                }
                create(LBFlavorDimension.Environment.Prod) {
                    dimension = LBFlavorDimension.Environment.Key
                    buildConfigField("Boolean", "IS_DEV", "false")
                }
                create(LBFlavorDimension.App.Client) {
                    dimension = LBFlavorDimension.App.Key
                    buildConfigField("Boolean", "IS_INTERNAL", "false")
                }
                create(LBFlavorDimension.App.Internal) {
                    dimension = LBFlavorDimension.App.Key
                    isDefault = true
                    buildConfigField("Boolean", "IS_INTERNAL", "true")
                }
            }
        }
    }
}
