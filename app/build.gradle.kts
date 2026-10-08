import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    //id "com.google.devtools.ksp"
}

fun getVersionNumber(): Int {
    val process = ProcessBuilder("python3", "app/get_version_number.py")
        .redirectErrorStream(true)
        .start()
    val inputReader = process.inputReader()

    val versionNumber = inputReader.readText().trim().toInt()

    inputReader.close()
    val exitCode = process.waitFor()

    if(exitCode != 0) {
        error("Failed to get version number")
    }
    return versionNumber
}

val versionNum = getVersionNumber()
val versionName = "v2.0.0-$versionNum-alpha1"

tasks.register("writeVersionFile") {
    description = "Writes version file for build"
    val outputFile = file("$projectDir/version.txt")
    doLast {
        outputFile.writeText("versionCode=$versionNum\nversionName=$versionName\n")
        println("Version file written to: $outputFile")
    }
}
tasks.named("preBuild") {
    dependsOn(tasks.named("writeVersionFile"))
}

android {
    namespace = "com.ammar.sharing"
    //noinspection GradleDependency
    compileSdk = 36
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    defaultConfig {
        applicationId = "com.ammar.sharing"
        minSdk = 23
        targetSdk = 36

        versionCode = versionNum
        versionName = versionName

        vectorDrawables.useSupportLibrary = true
        externalNativeBuild {
            cmake {
                abiFilters("arm64-v8a" ,"armeabi-v7a", "x86_64")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        prefab = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.core.ktx)

    implementation(libs.appcompat)
    implementation(libs.constraintlayout) // add this because we want negative margins
    implementation(libs.recyclerview) // when you add this recycler view width issue is fixed in dialogs
    implementation(libs.swiperefreshlayout) // needed to get CircularProgressDrawable
    implementation(libs.material)
    implementation(libs.glide)

    // I don't like the new library catalog declaration it makes you write more stuff :)
    implementation("com.github.zcweng:switch-button:0.0.3@aar")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.lifecycle:lifecycle-service:2.11.0")

    implementation("com.github.hendrawd:StorageUtil:1.1.0")
    implementation("io.getstream:stream-webrtc-android:1.3.10")
    implementation("org.jmdns:jmdns:3.6.3")

    implementation("androidx.navigation:navigation-fragment-ktx:2.10.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.10.2")

    implementation("org.bouncycastle:bcpkix-jdk18on:1.86")

    //ksp "androidx.room:room-compiler:2.5.0"
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("com.squareup.okhttp3:okhttp:5.5.0")


    implementation(project(":web"))

    testImplementation("junit:junit:4.13.2")
}