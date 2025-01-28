package studio.lunabee.plugins

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * This extension allows you to configure the LBDetekt plugin.
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
