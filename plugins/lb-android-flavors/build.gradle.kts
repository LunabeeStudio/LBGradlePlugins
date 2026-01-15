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
    implementation(libs.kotlinGradlePlugin)
}

description = "This plugin allows you to configure usual Android flavors in a simple and fast way."

gradlePlugin {
    plugins {
        create("studio.lunabee.plugin.android.flavors") {
            id = "studio.lunabee.plugin.android.flavors"
            implementationClass = "studio.lunabee.plugins.LBAndroidFlavorsPlugin"
            version = "1.1.1"
            displayName = "LBAndroidFlavors"
            description = project.description
            tags = listOf("android", "flavors", "lunabee")
        }
    }
}
