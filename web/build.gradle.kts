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

val jsonFileLocalProjectOptions = file("${rootDir}/local_project_options.json")
val localProjectOptions = if( jsonFileLocalProjectOptions.exists() ) {
    JsonSlurper().parseText(jsonFileLocalProjectOptions.readText())
} else {
    JsonSlurper().parseText("{}")
} as Map<*, *>

tasks.register<Exec>("buildWeb") {
    doFirst {
        println("Running buildWeb task...")

        println("Removing .parcel-cache/ if exists")
        file(".parcel-cache").deleteRecursively()

        println("Removing dist/ if exists")
        file("dist").deleteRecursively()
    }

    commandLine("pnpm", "run", "build")
    outputs.upToDateWhen {
        var forceRebuildWeb = localProjectOptions["force_rebuild_web"] as Boolean?
        if( forceRebuildWeb == null) {
            forceRebuildWeb = true
        }
        return@upToDateWhen !(forceRebuildWeb)
    }
}

tasks.named("preBuild") {
    doFirst {
        println("Running preBuild task...")
    }

    dependsOn(tasks.named("buildWeb"))
}
