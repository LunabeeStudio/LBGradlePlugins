import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import studio.lunabee.plugins.TargetPlatform

plugins {
    alias(libs.plugins.lbMultiplatformLibrary)
    alias(libs.plugins.lbDokka)
}

lbMultiplatformLibrary {
    multiplatform {
        jvmTarget = JvmTarget.JVM_21
        targets = listOf(
            TargetPlatform.Jvm(),
            TargetPlatform.Ios(),
        )
    }
}

dokka {
    moduleName.set("Multiplatform demo documentation")
}

dependencies {
    dokka(projects.appDemoMultiplatform)
    dokka(projects.appDemoShared)
}
