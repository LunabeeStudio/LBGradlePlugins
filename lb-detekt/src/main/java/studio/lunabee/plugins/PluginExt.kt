package studio.lunabee.plugins

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

internal val Project.libs
    get() = rootProject
        .extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

internal fun Project.getPluginId(name: String): String {
    return libs.findPlugin(name).get().get().pluginId
}

internal fun Project.applyPlugin(name: String) {
    plugins.apply(getPluginId(name))
}

internal fun Project.buildDependency(name: String): String {
    val lib = libs.findLibrary(name).get().get()
    return "${lib.group}:${lib.name}:${lib.version}"
}
