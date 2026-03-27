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
 * Last modified 1/12/26, 10:30 AM
 */

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
private val isPublishingToMavenLocal = gradle.startParameter.taskNames.any { taskName ->
    taskName == "publishToMavenLocal" || taskName.endsWith(":publishToMavenLocal")
}

private val stagingDir = layout.buildDirectory
    .dir("staging-deploy")
    .get()
    .asFile

signing {
    setRequired { !isPublishingToMavenLocal }
}

/* ============================================================
 * JReleaser configuration
 * ============================================================ */

jreleaser {
    gitRootSearch.set(true)

    deploy {
        maven {
            mavenCentral {
                create("release-deploy") {
                    active.set(org.jreleaser.model.Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository(stagingDir.path)
                    username.set(mavenCentralUsername)
                    password.set(mavenCentralPassword)
                    verifyPom.set(false) // FIXME https://github.com/jreleaser/jreleaser.github.io/issues/85
                    applyMavenCentralRules = false // FIXME https://github.com/jreleaser/jreleaser/issues/1746
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
                    verifyPom.set(false) // FIXME https://github.com/jreleaser/jreleaser.github.io/issues/85
                    applyMavenCentralRules = false // FIXME https://github.com/jreleaser/jreleaser/issues/1746
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
            setupSigning()
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
    groupId = project.group.toString()
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

private fun MavenPublication.setupSigning() {
    val signingKey: String? by project
    val signingPassword: String? by project
    if (!signingKey.isNullOrBlank()) {
        signing {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(this@setupSigning)
        }
    }
}

/* ============================================================
 * Tasks
 * ============================================================ */

tasks.register("PrintCoordinates") {
    val group = project.group.toString()
    val name = project.group.toString() + ".gradle.plugin"
    val version = project.version.toString()

    doLast {
        println("$group:$name:$version")
    }
}

private fun String.capitalized(): String =
    if (isEmpty()) this else this[0].titlecase(Locale.getDefault()) + substring(1)
