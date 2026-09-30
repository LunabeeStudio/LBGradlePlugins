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
 * Created by Lunabee Studio / Date - 9/30/2026
 * Last modified 9/30/26, 10:00 AM
 */

plugins {
    `java-library`
    kotlin("jvm")
    id("lunabee.publish-conventions")
}

dependencies {
    compileOnly(libs.detektApi)

    testImplementation(libs.detektApi)
    testImplementation(libs.detektTest)
    testImplementation(libs.kotlinTest)
}

description = "Lunabee custom Detekt rules, loaded by the studio.lunabee.plugin.detekt plugin."
group = "studio.lunabee.plugin.detekt"
version = "1.0.0-beta01"

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("PrintCoordinates") {
    val group = project.group.toString()
    val name = project.name
    val version = project.version.toString()

    doLast {
        println("$group:$name:$version")
    }
}
