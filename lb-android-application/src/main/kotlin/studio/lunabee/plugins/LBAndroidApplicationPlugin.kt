@file:Suppress("unused")

package studio.lunabee.plugins

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import studio.lunabee.plugins.LBAndroidApplicationExtension.PropertiesReceiver
import javax.inject.Inject

/**
 * This extension allows you to fully configure your Android application.
 * It sets up some default information. Refer to the [PropertiesReceiver] class for more details.
 */
open class LBAndroidApplicationExtension @Inject constructor(private val project: Project) {
    private val propertiesReceiver = PropertiesReceiver()

    /**
     * @param applicationId The unique application ID for your Android app. Error will be thrown is not set.
     * @param namespace The namespace for your application. Error will be thrown is not set.
     * @param compileSdk The SDK version used to compile the application. Default is 35.
     * @param buildToolsVersion The version of the Android Build Tools to be used. Default is "35.0.0".
     * @param minSdk The minimum SDK version required to run the application. Default is 23.
     * @param targetSdk The target SDK version the application is optimized for. Default is the same as [compileSdk].
     * @param versionCode The version code of the application, used for versioning on the Play Store.
     * @param versionName The version name of the application, displayed to the users.
     * @param jdkVersion The version of the Java Development Kit (JDK) to be used. Default is [JavaVersion.VERSION_21].
     * @param configureJava A lambda for additional configuration of the Java plugin extension.
     * @param configureAndroidExtension A lambda for additional configuration of the Android application module extension.
     * @param lintConfigPath File path to lint config you might want to add.
     */
    data class PropertiesReceiver(
        var applicationId: String = "",
        var namespace: String = "",
        var compileSdk: Int = 35,
        var buildToolsVersion: String = "35.0.0",
        var minSdk: Int = 23,
        var targetSdk: Int = compileSdk,
        var versionCode: Int = 1,
        var versionName: String = "1.0.0",
        var jdkVersion: JavaVersion = JavaVersion.VERSION_21,
        var configureJava: JavaPluginExtension.() -> Unit = { },
        var configureAndroidExtension: BaseAppModuleExtension.() -> Unit = { },
        var lintConfigPath: String? = null,
    )

    /**
     * Must be called to configure Android plugin correctly. You must set, at least [PropertiesReceiver.applicationId] and
     * [PropertiesReceiver.namespace]. Other params will fallback on default value.
     */
    fun android(block: PropertiesReceiver.() -> Unit) {
        block(propertiesReceiver)
        init()
    }

    @Suppress("LongMethod")
    private fun init() {
        // Configure Java Toolchain.
        project.extensions.configure<JavaPluginExtension>("java") {
            toolchain { languageVersion.set(JavaLanguageVersion.of(propertiesReceiver.jdkVersion.toString())) }
            propertiesReceiver.configureJava(this)
        }

        project.extensions.configure<BaseAppModuleExtension>("android") {
            // Set Android parameters from [propertiesReceiver] object.
            namespace = propertiesReceiver.namespace
            compileSdk = propertiesReceiver.compileSdk
            buildToolsVersion = propertiesReceiver.buildToolsVersion

            // Set default configuration parameters from [propertiesReceiver] object.
            defaultConfig.applicationId = propertiesReceiver.applicationId
            defaultConfig.versionCode = propertiesReceiver.versionCode
            defaultConfig.versionName = propertiesReceiver.versionName
            defaultConfig.targetSdk = propertiesReceiver.targetSdk
            defaultConfig.minSdk = propertiesReceiver.minSdk

            // Set compile options (i.e Java version) from [propertiesReceiver] object.
            compileOptions.sourceCompatibility = propertiesReceiver.jdkVersion
            compileOptions.targetCompatibility = propertiesReceiver.jdkVersion

            // Enable buildConfig feature (to have access to Build variable) and compose (for previews).
            buildFeatures.buildConfig = true
            buildFeatures.compose = true

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

            // Configure signing config for debug. [debug.keystore] file must be located at root level.
            signingConfigs {
                maybeCreate("debug").apply {
                    storeFile = project.rootProject.file("debug.keystore")
                    storePassword = "androiddebug"
                    keyAlias = "debug"
                    keyPassword = "androiddebug"
                }
            }

            // Configure lint option.
            propertiesReceiver.lintConfigPath?.let { lintConfigPath ->
                lint {
                    lintConfig = project.rootProject.file(lintConfigPath)
                    baseline = project.rootProject.file("lint-baseline.xml")
                    disable.add("ObsoleteLintCustomCheck")
                    htmlOutput = project.rootProject.file("${project.rootProject.projectDir}/build/reports/lint/lint-report.html")
                    xmlOutput = project.rootProject.file("${project.rootProject.projectDir}/build/reports/lint/lint-report.xml")
                    textOutput = project.rootProject.file("${project.rootProject.projectDir}/build/reports/lint/lint-report.txt")
                }
            }

            // Configuration from release and debug.
            buildTypes {
                debug {
                    isMinifyEnabled = false
                    println(signingConfigs.getByName("debug"))
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

            propertiesReceiver.configureAndroidExtension(this)
        }
    }
}

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
        target.extensions.create("lbAndroidApplication", LBAndroidApplicationExtension::class.java)
        target.applyPlugin("gradleAndroidApplication")
        target.applyPlugin("kotlinAndroid")
        target.applyPlugin("kotlinCompose")
    }
}
