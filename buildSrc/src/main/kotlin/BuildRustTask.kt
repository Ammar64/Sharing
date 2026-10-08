import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class BuildRustTask : DefaultTask() {
    @get:Internal
    abstract val rustProjectPath: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val rustSrcFiles: FileTree
        get() = rustProjectPath.asFileTree.matching { exclude("target/**") }

    @get:OutputDirectory
    abstract val outJniLibDir: DirectoryProperty

    @get:Input
    abstract val release: Property<Boolean>

    @get:Input
    abstract val abis: ListProperty<String>


    @get:Input
    abstract val minSdkVer: Property<Int>

    @get:Inject
    abstract val execOps: ExecOperations

    @TaskAction
    fun build() {
        val targetABIs = abis.get()
        val cargoArgs = mutableListOf("cargo", "ndk", "--platform", minSdkVer.get().toString())
        targetABIs.forEach { abi -> cargoArgs += listOf("-t", abi) }
        cargoArgs += listOf("-o", outJniLibDir.get().toString())
        cargoArgs += "build"
        if (release.get()) cargoArgs += "--release"

        // Remove any previously-built ABI folder that isn't part of this build
        outJniLibDir.get().files().forEach { abiFolder ->
            if (abiFolder.isDirectory && abiFolder.name !in targetABIs) {
                abiFolder.deleteRecursively()
            }
        }

        execOps.exec {
            workingDir = rustProjectPath.get().asFile
            commandLine(cargoArgs)
        }.assertNormalExitValue()

    }
}