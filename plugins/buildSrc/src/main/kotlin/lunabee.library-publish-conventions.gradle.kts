/*
 * lunabee.gradle-plugin-publish-conventions.gradle.kts
 */

import com.github.javaparser.printer.concretesyntaxmodel.CsmElement.token
import gradle.kotlin.dsl.accessors._eb2e459b1bd68d394f66bc4c88d90a6f.jreleaser
import gradle.kotlin.dsl.accessors._eb2e459b1bd68d394f66bc4c88d90a6f.signing
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.`java-gradle-plugin`
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.model.internal.core.ModelNodes.withType
import org.jreleaser.model.Signing
import studio.lunabee.VersionTask
import java.util.Locale

plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("org.jreleaser")
    id("com.gradle.plugin-publish")
}

/* ============================================================
 * Credentials & staging
 * ============================================================ */

private val mavenCentralUsername = project.properties["mavenCentralUsername"]?.toString()
private val mavenCentralPassword = project.properties["mavenCentralPassword"]?.toString()

private val stagingDir = layout.buildDirectory
    .dir("staging-deploy")
    .get()
    .asFile

/* ============================================================
 * Gradle Plugin definition
 * ============================================================ */

gradlePlugin {
    plugins {
        create("lunabeePlugin") {
            id = "studio.lunabee.${project.name}"
            implementationClass = "studio.lunabee.${project.name}.PluginEntryPoint"
            displayName = project.name.capitalized()
            description = project.description
        }
    }
}

/* ============================================================
 * JReleaser configuration
 * ============================================================ */

jreleaser {
    gitRootSearch.set(true)

    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        pgp {
            armored.set(true)
            mode.set(Signing.Mode.FILE)
        }
    }

    deploy {
        maven {
            mavenCentral {
                create("release-deploy") {
                    active.set(org.jreleaser.model.Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository(stagingDir.path)
                    username.set(mavenCentralUsername)
                    password.set(mavenCentralPassword)
                    verifyPom.set(false)
                }
            }
            nexus2 {
                create("snapshot-deploy") {
                    active.set(org.jreleaser.model.Active.SNAPSHOT)
                    url.set("https://central.sonatype.com/repository/maven-snapshots")
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots")
                    applyMavenCentralRules = true
                    snapshotSupported = true
                    closeRepository = true
                    releaseRepository = true
                    username.set(mavenCentralUsername)
                    password.set(mavenCentralPassword)
                    verifyPom.set(false)
                    stagingRepository(stagingDir.path)
                }
            }
        }
    }

    release {
        github {
            token.set("fake")
            skipRelease.set(true)
            skipTag.set(true)
        }
    }
}

/* ============================================================
 * Publishing configuration
 * ============================================================ */

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            setProjectDetails()
            setPom()
        }
    }

    repositories {
        maven {
            url = uri(stagingDir)
        }
    }
}

/* ============================================================
 * Maven Publication helpers
 * ============================================================ */

fun MavenPublication.setProjectDetails() {
    groupId = "studio.lunabee.plugin"
    artifactId = project.name
    version = project.version.toString()
}

fun MavenPublication.setPom() {
    val pluginRepoUrl = "https://github.com/LunabeeStudio/LBGradlePlugins"
    val pluginRepoSsh = "git@github.com:LunabeeStudio/LBGradlePlugins.git"
    pom {
        name.set(project.name.capitalized())
        description.set(project.description)
        url.set(pluginRepoUrl)

        organization {
            name.set("Lunabee Studio")
            url.set("https://www.lunabee.studio")
        }

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        scm {
            connection.set(pluginRepoSsh)
            developerConnection.set(pluginRepoSsh)
            url.set(pluginRepoUrl)
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

/* ============================================================
 * Signing
 * ============================================================ */

signing {
    setRequired {
        gradle.taskGraph.hasTask("publish")
    }
    sign(publishing.publications)
}

/* ============================================================
 * Tasks
 * ============================================================ */

tasks.register("${project.name}Version", VersionTask::class.java)

private fun String.capitalized(): String =
    if (isEmpty()) this else this[0].titlecase(Locale.getDefault()) + substring(1)
