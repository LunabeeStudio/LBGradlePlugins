package studio.lunabee.plugins

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import javax.inject.Inject

private const val DefaultCompileSdk: Int = 35
private const val DefaultBuildToolsVersion: String = "35.0.0"
private const val DefaultMinSdk: Int = 23
private const val DefaultVersionCode: Int = 1
private const val DefaultVersionName: String = "1.0.0"
private val DefaultJdkVersion: JavaVersion = JavaVersion.VERSION_17

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
        project.extensions.configure<BaseAppModuleExtension>("android") {
            compileSdk = DefaultCompileSdk
            buildToolsVersion = DefaultBuildToolsVersion

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

    fun android(block: BaseAppModuleExtension.() -> Unit) {
        project.extensions.configure<BaseAppModuleExtension>("android") {
            block()
        }
    }

    fun java(block: JavaPluginExtension.() -> Unit) {
        project.extensions.configure<JavaPluginExtension>("java") {
            block()
        }
    }
}
