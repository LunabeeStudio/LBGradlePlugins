package studio.lunabee.plugins

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import javax.inject.Inject

/**
 * This extension allows you to fully configure an Android library module.
 */
open class LBAndroidLibraryExtension @Inject constructor(private val project: Project) {

    fun android(block: LibraryExtension.() -> Unit) {
        project.extensions.configure<LibraryExtension> {
            block()
            if (buildFeatures.compose == true) {
                println("Apply compose compiler")
                project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            }
        }
    }
}
