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
 * Last modified 12/17/25, 10:14 AM
 */

package studio.lunabee.plugins

import org.gradle.api.Project
import java.io.File
import javax.inject.Inject

/**
 * Register a task to download all the strings from your provider.
 *
 * @property targetDirectory directory where your `src` target directory can be found. Default to project path
 * @property provider provider (currently, we only handle [StringsProvider.Loco]).
 * @property replaceQuotes true if you want to replace \" by “
 * @property replaceApostrophes true if you want to replace \' by ’ or ‘
 */
open class LBResourcesExtension @Inject constructor(project: Project) {
    var targetDirectory: File = project.projectDir
    var provider: StringsProvider? = null
    var replaceQuotes: Boolean = true // true by default to not break existing project if not desired
    var replaceApostrophes: Boolean = true // true by default to not break existing project if not desired
}
