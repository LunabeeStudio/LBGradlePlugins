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

@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("lunabee.plugin-conventions")
}

dependencies {
    implementation(libs.gradleAndroid)
    implementation(libs.kotlinCompose)
    implementation(libs.kotlinGradlePlugin)
}

gradlePlugin {
    plugins {
        create("studio.lunabee.plugins.android.library") {
            id = "studio.lunabee.plugins.android.library"
            implementationClass = "studio.lunabee.plugins.LBAndroidLibraryPlugin"
            version = "1.1.0"
            displayName = "LBAndroidLibrary"
            description = "This plugin allows you to configure an Android library in a simple and fast way."
            tags = listOf("android", "library", "lunabee")
        }
    }
}
