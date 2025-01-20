import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import studio.lunabee.plugins.TargetPlatform

plugins {
    alias(libs.plugins.lbMultiplatformLibrary)
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
