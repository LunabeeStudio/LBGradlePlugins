/*
 * Copyright (c) 2023 Lunabee Studio
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
 */

import java.net.URI
import java.util.Locale

plugins {
    id("com.gradle.plugin-publish")
}

project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
    website = "https://lunabee.studio"
    vcsUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"
}

project.extensions.configure<PublishingExtension>("publishing") {
    setupMavenRepository()
    publications {
        create<MavenPublication>("pluginMaven") {
            setPom()
        }
    }
}

/**
 * Set repository destination depending on [project] and version name.
 * Credentials should be stored in your root gradle.properties, in a non source controlled file.
 */
fun PublishingExtension.setupMavenRepository() {
    repositories {
        maven {
            authentication {
                credentials.username = System.getenv(EnvConfig.ENV_ARTIFACTORY_USER)
                    ?: project.properties["artifactory_deployer_release_username"] as? String
                credentials.password = System.getenv(EnvConfig.ENV_ARTIFACTORY_API_KEY)
                    ?: project.properties["artifactory_deployer_release_api_key"] as? String
            }
            url = URI.create("https://artifactory.lunabee.studio/artifactory/lunabee-gradle-plugin")
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
