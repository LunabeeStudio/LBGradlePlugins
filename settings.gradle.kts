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
 * Last modified 9/23/25, 3:41 PM
 */

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        includeBuild("plugins")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("studio.lunabee.plugin.cache") version "1.0.0"
}

rootProject.name = "LBGradlePluginsDemo"
include(":app")
project(":app").projectDir = File("./demo/app")
include(":app-demo-core-ui")
project(":app-demo-core-ui").projectDir = File("./demo/app-demo-core-ui")
include(":app-demo-multiplatform")
project(":app-demo-multiplatform").projectDir = File("./demo/app-demo-multiplatform")
include(":app-demo-shared")
project(":app-demo-shared").projectDir = File("./demo/app-demo-shared")
include(":docs")
