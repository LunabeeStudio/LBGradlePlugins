plugins {
    `kotlin-dsl`
    `java-library`
    `maven-publish`
}

group = "studio.lunabee.plugins"
version = "1.0.0"
description = "A set of extensions and method that can be used accross all others plugins."

publishing {
    publications {
        create<MavenPublication>("lb-plugin-core") {
            groupId = project.group.toString()
            artifactId = "core"
            version = project.version.toString()

            pom {
                name.set("LBPluginCore")
                description.set(project.description)
                url.set("https://lunabee.studio")

                organization {
                    name.set("Lunabee Studio")
                    url.set("https://lunabee.studio")
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
    }
}
