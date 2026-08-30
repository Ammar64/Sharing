import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class BuildRustTask : DefaultTask() {
    @get:InputFiles
    abstract val rustSrcFiles: Property<ConfigurableFileTree>

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
            workingDir = rustSrcFiles.get().dir
            commandLine(cargoArgs)
        }.assertNormalExitValue()

    }
}