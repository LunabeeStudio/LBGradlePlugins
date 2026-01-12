/*
 * Copyright (c) 2023-2026 Lunabee Studio
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
 * Last modified 9/25/25, 10:11 AM
 */

import java.util.Locale

plugins {
    id("com.gradle.plugin-publish")
}

project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"
}

project.extensions.configure<PublishingExtension>("publishing") {
    publications {
        create<MavenPublication>("pluginMaven") {
            setPom()
        }
    }
}

/**
 * Set POM file details.
 */
fun MavenPublication.setPom() {
    pom {
        name.set(project.name.capitalized())
        description.set(project.description)
        url.set("https://github.com/LunabeeStudio/LBGradlePlugins")

        organization {
            name.set("Lunabee Studio")
            url.set("https://www.lunabee.studio")
        }

        scm {
            connection.set("git@github.com:LunabeeStudio/LBGradlePlugins.git")
            developerConnection.set("git@github.com:LunabeeStudio/LBGradlePlugins.git")
            url.set("https://github.com/LunabeeStudio/LBGradlePlugins")
        }

        developers {
            developer {
                id.set("Publisher")
                name.set("Publisher Lunabee")
                email.set("publisher@lunabee.com")
            }
        }
    }
}

private fun String.capitalized(): String = if (this.isEmpty()) this else this[0].titlecase(Locale.getDefault()) + this.substring(1)
