plugins {
    `kotlin-dsl`
    `maven-publish`
}

version = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("lb-plugin-core") {
            groupId = "studio.lunabee.plugins"
            artifactId = "core"
            version = "1.0.0"

            pom {
                name.set("LBPluginCore")
                description.set("A set of extensions and method that can be used accross all others plugins.")
                url.set("https://www.lunabee.studio")

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
    }
}
