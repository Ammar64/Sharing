import groovy.json.JsonSlurper

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

        val buildWebTask = tasks.register<BuildWebTask>("buildWeb$variantCap") {
            frontendDir.set(fileTree("frontend") {
                exclude("node_modules/**")
            })
            outputDirectory.set(layout.projectDirectory.dir("dist"))
        }

        variant.sources.assets?.addGeneratedSourceDirectory(
            buildWebTask,
            BuildWebTask::outputDirectory
        )
    }
}