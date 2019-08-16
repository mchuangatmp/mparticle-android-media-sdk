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
    mavenCentral()
    mavenLocal()
    google()
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    implementation("com.mparticle:android-core:5.9.7-SNAPSHOT")
    implementation("com.mparticle:android-kit-base:5.9.7-SNAPSHOT")

    testImplementation("junit:junit:4.12")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
}