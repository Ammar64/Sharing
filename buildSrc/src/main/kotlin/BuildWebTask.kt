import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class BuildWebTask : DefaultTask() {
    @get:InputFiles
    abstract val frontendDir: Property<ConfigurableFileTree>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val execOps: ExecOperations

    @TaskAction
    fun build() {
        execOps.exec {
            workingDir = frontendDir.get().dir
            commandLine("pnpm", "run", "build")
        }.assertNormalExitValue()
    }
}