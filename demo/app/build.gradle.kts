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
 * Last modified 1/12/26, 9:42 AM
 */

import studio.lunabee.plugins.StringsProvider

plugins {
    alias(libs.plugins.lbAndroidApplication)
    alias(libs.plugins.lbAndroidFlavors)
    alias(libs.plugins.lbResources)
}

lbAndroidApplication {
    android {
        namespace = "studio.lunabee.lbplugin"
        compileSdk = 36
        defaultConfig {
            minSdk = 26
            versionCode = 1
            versionName = "1.0.0"
        }
    }
}

lbResources {
    provider = StringsProvider.Loco("maZteRAXR20kWa9XBlOfZ7Pgnwy8uor3")
}

dependencies {
    implementation(platform(libs.androidxComposeBom))

    implementation(libs.androidxActivityCompose)
    implementation(libs.androidxAppCompat)
    implementation(libs.androidxComposeFoundation)
    implementation(libs.androidxComposeMaterial3)

    implementation(projects.appDemoCoreUi)
    implementation(projects.appDemoMultiplatform)
    implementation(projects.appDemoShared)

    devImplementation(projects.appDemoShared)
}
