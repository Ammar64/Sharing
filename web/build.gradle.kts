plugins {
    id("com.android.library")
}
android {
    defaultConfig {
        namespace = "com.ammar.sharing.web"
        compileSdk = 34
    }
    sourceSets {
        getByName("main").assets.directories.add("dist")
    }
}


androidComponents {
    onVariants { variant ->
        val variantCap = variant.name.replaceFirstChar { it.uppercase() }

        val srcDir = layout.projectDirectory.dir("src")
        val buildWebTask = tasks.register<BuildWebTask>("buildWeb$variantCap") {
            description = "Build web side of the app"
            webProjectPath.set(srcDir)
            outputDirectory.set(srcDir.dir("dist"))
        }

        variant.sources.assets?.addGeneratedSourceDirectory(
            buildWebTask,
            BuildWebTask::outputDirectory
        )
    }
}
