import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import studio.lunabee.plugins.TargetPlatform

plugins {
    alias(libs.plugins.lbMultiplatformLibrary)
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
            iosMain.dependencies {
                // Common dependencies goes here.
            }
            // iOS dependencies goes here.
        }
        jvmMain.dependencies {
            // jvm dependencies goes here.
        }
    }
}
