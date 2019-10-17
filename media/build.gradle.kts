plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
}

ext["kitDescription"] = "Media Api to supplement core SDK"

apply(from= "../scripts/maven.gradle")

android {
    defaultConfig {
        compileSdkVersion(28)
    }
}

repositories {
    jcenter()
    mavenLocal()
    google()
}


dependencies {
    testImplementation(files("libs/test-utils.aar"))
    testImplementation("junit:junit:4.12")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    implementation("com.mparticle:android-core:5.12.0-SNAPSHOT")
}