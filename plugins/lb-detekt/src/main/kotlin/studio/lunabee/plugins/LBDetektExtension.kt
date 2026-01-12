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
 * Last modified 9/25/25, 10:11 AM
 */

package studio.lunabee.plugins

import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * This extension allows you to configure the LBDetekt plugin. It extends [DetektExtension] to make the Detekt config available directly.
 *
 * @property verbose Enable debug logs. Default false
 * @property lunabeeConfig Default Lunabee configuration file
 * @property enableDependencySorting Run [SortBuildDependenciesTask] before detekt. Default true
 * @property enableTomlSorting Run [SortLibsVersionsTomlTask] before detekt. Default true
 */
abstract class LBDetektExtension(detektExtension: DetektExtension) : DetektExtension by detektExtension {
    abstract val verbose: Property<Boolean>
    abstract val lunabeeConfig: RegularFileProperty
    abstract val enableDependencySorting: Property<Boolean>
    abstract val enableTomlSorting: Property<Boolean>
}
