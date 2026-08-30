plugins {
    alias(libs.plugins.androidLibrary)
}

val verCode: Int by rootProject.extra
val verName: String by rootProject.extra
val androidMinSdkVersion: Int by rootProject.extra
val androidTargetABIs: List<String> by rootProject.extra
android {
    namespace = "com.ammar.sharing.nativebackend"

    buildFeatures {
        buildConfig = false
    }
    androidResources.enable = false

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = androidMinSdkVersion

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.core.ktx)
}

androidComponents {
    onVariants { variant ->
        val variantCap = variant.name.replaceFirstChar { it.uppercase() }
        val isReleaseBuild = variant.buildType == "release"

        val rustSrcDir = layout.projectDirectory.dir("src/main/rust")
        val jniLibsDir = layout.projectDirectory.dir("src/main/rustOutput")

        val injectedAbi = project.providers.gradleProperty("android.injected.build.abi").orNull
        val targetABIs = if (isReleaseBuild || injectedAbi == null) {
            androidTargetABIs
        } else {
            injectedAbi.split(",").filter { it in androidTargetABIs } // debug: only what Studio wants
        }

        val rustTask = tasks.register<BuildRustTask>("buildRust$variantCap") {
            group = "rust"
            description = "Builds the Rust library for the $variantCap variant"

            rustSrcFiles.set(
                fileTree(rustSrcDir) {
                    exclude("target/**") // exclude cargo's own build output if it lands inside this folder
                }
            )

            outJniLibDir.set(jniLibsDir)
            release.set(isReleaseBuild)
            abis.set(targetABIs)

            minSdkVer.set(androidMinSdkVersion)
        }

        variant.sources.jniLibs?.addGeneratedSourceDirectory(
            rustTask,
            BuildRustTask::outJniLibDir
        )
    }
}


