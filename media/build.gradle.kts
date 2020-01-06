import org.sonarqube.gradle.SonarQubeExtension

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    id("org.sonarqube").version("2.7")

}

ext["kitDescription"] = "Media Api to supplement core SDK"

apply(from= "../scripts/maven.gradle")

android {
    defaultConfig {
        minSdkVersion(16)
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
    testImplementation(files("libs/java-json.jar"))
    testImplementation("junit:junit:4.12")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    implementation("com.mparticle:android-core:[5.11.3,)")

}
configure<SonarQubeExtension> {
    properties {
        property("sonar.projectName", "Android")
        property("sonar.prjectKey", "Android")
    }
}