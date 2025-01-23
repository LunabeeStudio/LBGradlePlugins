package studio.lunabee.plugins

import com.android.build.gradle.LibraryExtension
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
     * @param compileSdk The SDK version used to compile the library. Default is 35.
     * @param withCompose Whether to apply the Compose plugin. Default is false.
     * @param applyKotlinAndroid Whether to apply the Kotlin Android plugin. Default is true.
     * @param enableBuildConfig Whether to enable access to build config field. Default is false.
     * @param compileSdk The SDK version used to compile the application. Default is 35.
     * @param buildToolsVersion The version of the Android Build Tools to be used. Default is "35.0.0".
     * @param minSdk The minimum SDK version required to run the application. Default is 23.
     * @param jdkVersion The version of the Java Development Kit (JDK) to be used. Default is [JavaVersion.VERSION_17].
     * @param configureJava A lambda for additional configuration of the Java plugin extension.
     * @param configureAndroidExtension A lambda for additional configuration of the Android application module extension.
     */
    data class PropertiesReceiver(
        var namespace: String = "",
        var compileSdk: Int = 35,
        var withCompose: Boolean = false,
        var applyKotlinAndroid: Boolean = true,
        var enableBuildConfig: Boolean = false,
        var buildToolsVersion: String = "35.0.0",
        var minSdk: Int = 23,
        var jdkVersion: JavaVersion = JavaVersion.VERSION_17,
        var configureJava: JavaPluginExtension.() -> Unit = { },
        var configureAndroidExtension: LibraryExtension.() -> Unit = { },
    )

    fun android(block: PropertiesReceiver.() -> Unit) {
        block(propertiesReceiver)
        init()
    }

    private fun init() {
        // Apply compose plugin if applicable.
        if (propertiesReceiver.withCompose) project.applyPlugin("kotlinCompose")
        // Apply Kotlin Android plugin if applicable.
        if (propertiesReceiver.applyKotlinAndroid) project.applyPlugin("kotlinAndroid")

        // Configure Java Toolchain.
        project.extensions.configure<JavaPluginExtension>("java") {
            toolchain { languageVersion.set(JavaLanguageVersion.of(propertiesReceiver.jdkVersion.toString())) }
            propertiesReceiver.configureJava(this)
        }

        project.extensions.configure<LibraryExtension>("android") {
            // Set Android parameters from [propertiesReceiver] object.
            namespace = propertiesReceiver.namespace
            compileSdk = propertiesReceiver.compileSdk
            buildToolsVersion = propertiesReceiver.buildToolsVersion

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