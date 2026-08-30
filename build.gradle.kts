// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    //id 'com.google.devtools.ksp' version '2.0.21-1.0.27' apply false
}


val verCode by extra(Utils.getVersionNumber())
val verName by extra("v2.0.0-$verCode-alpha1")

val androidMinSdkVersion  by extra(23)
val androidTargetABIs by extra(listOf(
    "arm64-v8a",
    "armeabi-v7a",
    "x86_64"
))