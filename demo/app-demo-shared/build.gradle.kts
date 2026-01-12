/*
 * Copyright (c) 2026 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 1/12/2026
 * Last modified 1/9/26, 5:10 PM
 */

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
    }
}

lbMultiplatformLibrary {
    multiplatform {
        jvmTarget = JvmTarget.JVM_21
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
