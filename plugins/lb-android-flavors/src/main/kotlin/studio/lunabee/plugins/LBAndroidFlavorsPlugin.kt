@file:Suppress("unused")

package studio.lunabee.plugins

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
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
        target.extensions.configure<BaseAppModuleExtension>("android") {
            // Configure flavors for the app: dev, prod and client, internal. By default, devInternal will be used.
            flavorDimensions += "environment"
            flavorDimensions += "app"
            productFlavors {
                create("dev") {
                    dimension = "environment"
                    applicationIdSuffix = ".dev"
                    versionNameSuffix = "#$versionCode dev"
                    isDefault = true
                    buildConfigField("Boolean", "IS_DEV", "true")
                }
                create("prod") {
                    dimension = "environment"
                    buildConfigField("Boolean", "IS_DEV", "false")
                }
                create("client") {
                    dimension = "app"
                    buildConfigField("Boolean", "IS_INTERNAL", "false")
                }
                create("internal") {
                    dimension = "app"
                    isDefault = true
                    buildConfigField("Boolean", "IS_INTERNAL", "true")
                }
            }
        }
    }
}
