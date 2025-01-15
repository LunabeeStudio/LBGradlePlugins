package studio.lunabee.plugins

import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

sealed interface TargetPlatform {
    data class Ios(
        val frameworkConfig: Framework.() -> Unit = { },
    ) : TargetPlatform

    data class Android(
        val configure: KotlinAndroidTarget.() -> Unit = { },
    ) : TargetPlatform

    data class Jvm(
        val configure: KotlinJvmTarget.() -> Unit = { },
    ) : TargetPlatform
}
