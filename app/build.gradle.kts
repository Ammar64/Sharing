import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
}

val verCode: Int by rootProject.extra
val verName: String by rootProject.extra
val androidMinSdkVersion: Int by rootProject.extra
val androidTargetABIs: List<String> by rootProject.extra

tasks.register("writeVersionFile") {
    description = "writes version name and version code to a file for F-Droid"
    val outputFile = file("$projectDir/version.txt")
    doLast {
        outputFile.writeText(
            """\
versionCode=${verCode}
versionName=${verName}
""".trimIndent()
        )
        println("Version file written to: $outputFile")
    }
}
tasks.named("preBuild") {
    dependsOn(tasks.named("writeVersionFile"))
}

android {
    namespace = "com.ammar.sharing"
    //noinspection GradleDependency
    compileSdk = 37
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    defaultConfig {
        applicationId = "com.ammar.sharing"
        minSdk = androidMinSdkVersion
        targetSdk = 35

        versionCode = verCode
        versionName = verName

        vectorDrawables.useSupportLibrary = true
        externalNativeBuild {
            cmake {
                abiFilters += androidTargetABIs
            }
        }

        // tells AGP to only include those ABIs in the final APK
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += androidTargetABIs
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    annotationProcessor(libs.glide.compiler)

    // I don't like the new library catalog declaration it makes you write more stuff :)
    implementation("com.github.zcweng:switch-button:0.0.3@aar")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.lifecycle:lifecycle-service:2.9.3")

    implementation("com.github.hendrawd:StorageUtil:1.1.0")
    implementation("io.getstream:stream-webrtc-android:1.3.8")
    implementation("org.jmdns:jmdns:3.6.3")

    implementation("androidx.navigation:navigation-fragment-ktx:2.9.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.3")

    implementation("org.bouncycastle:bcpkix-jdk18on:1.81")

    //ksp "androidx.room:room-compiler:2.5.0"
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")


    implementation(project(":web"))
    implementation(project(":nativebackend"))

    testImplementation("junit:junit:4.13.2")
}