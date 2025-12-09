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
