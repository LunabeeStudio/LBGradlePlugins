import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import studio.lunabee.plugins.TargetPlatform

plugins {
    alias(libs.plugins.lbMultiplatformLibrary)
    alias(libs.plugins.lbDokka)
}

lbMultiplatformLibrary {
    multiplatform {
        jvmTarget = JvmTarget.JVM_17
        targets = listOf(
            TargetPlatform.Jvm(),
            TargetPlatform.Ios(),
        )
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Common dependencies goes here.
        }
        iosMain.dependencies {
            // iOS dependencies goes here.
        }
        jvmMain.dependencies {
            // jvm dependencies goes here.
        }
    }
}
