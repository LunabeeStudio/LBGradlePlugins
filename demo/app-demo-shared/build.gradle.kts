import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import studio.lunabee.plugins.TargetPlatform

plugins {
    alias(libs.plugins.lbMultiplatformLibrary)
    alias(libs.plugins.lbAndroidLibrary)
    alias(libs.plugins.lbDokka)
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

lbDokka {
    docProject = "docs"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // Android dependencies goes here.
        }
        commonMain.dependencies {
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
