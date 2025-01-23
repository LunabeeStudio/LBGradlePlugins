import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import studio.lunabee.plugins.TargetPlatform

plugins {
    alias(libs.plugins.lbMultiplatformLibrary)
    alias(libs.plugins.lbAndroidLibrary)
}

lbAndroidLibrary {
    android {
        namespace = "studio.lunabee.plugin.demo.shared"
        withCompose = false
        applyKotlinAndroid = false
    }
}

lbMultiplatformLibrary {
    multiplatform {
        jvmTarget = JvmTarget.JVM_17
        targets = listOf(
            TargetPlatform.Android(),
            TargetPlatform.Jvm(),
            TargetPlatform.Ios {
                export(projects.appDemoMultiplatform)
                isStatic = true
            },
        )
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            commonMain.dependencies {
                // Android dependencies goes here.
            }

            implementation(projects.appDemoMultiplatform)
        }
        iosMain.dependencies {
            api(projects.appDemoMultiplatform)
        }
        jvmMain.dependencies {
            // Jvm dependencies goes here.
        }
    }
}
