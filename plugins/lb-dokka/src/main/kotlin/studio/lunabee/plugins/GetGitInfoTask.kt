package studio.lunabee.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Extracts Git branch and repository information and writes it to a file.
 * Requires git binary accessible and Git remote named "origin"
 *
 * @property branchFileProvider The output file containing the branch (line 0) and repository name (line 1)
 */
abstract class GetGitInfoTask @Inject constructor(
    project: Project,
    private val execOperations: ExecOperations,
) : DefaultTask() {

    @get:OutputFile
    val branchFileProvider: Provider<RegularFile> = project.layout.buildDirectory.file("git/info")

    @TaskAction
    fun printInfo() {
        val infoFile = branchFileProvider.get().asFile
        infoFile.delete()
        infoFile.parentFile.mkdirs()
        infoFile.createNewFile()

        val branchStdOut = ByteArrayOutputStream()
        execOperations.exec {
            standardOutput = branchStdOut
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        }

        val urlStdOut = ByteArrayOutputStream()
        execOperations.exec {
            standardOutput = urlStdOut
            commandLine("git", "remote", "get-url", "origin")
        }

        val rootDir = ByteArrayOutputStream()
        execOperations.exec {
            standardOutput = rootDir
            commandLine("git", "rev-parse", "--show-toplevel")
        }

        infoFile.appendText(branchStdOut.toString(Charsets.UTF_8).lines().first())
        infoFile.appendText("\n")
        val repoUrl = urlStdOut.toString(Charsets.UTF_8)
        val startRepositoryNameIndex = repoUrl.indexOfAny(charArrayOf(':', '/')) + 1
        infoFile.appendText(repoUrl.substring(startRepositoryNameIndex).substringBefore('.'))
        infoFile.appendText("\n")
        infoFile.appendText(rootDir.toString(Charsets.UTF_8).lines().first())
    }
}
