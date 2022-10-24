plugins {
    id("com.android.library")
    kotlin("android")
}

ext["kitDescription"] = "Media Api to supplement core SDK"

apply(from= "../.scripts/maven.gradle")

android {
    namespace = "com.mparticle.media"
    compileSdk = 33
    defaultConfig {
        minSdk = 16
        targetSdk = 33
    }
    lint {
        abortOnError = true
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    implementation("com.mparticle:android-core:[5.11.3,)")

    testImplementation(files("libs/test-utils.aar"))
    testImplementation(files("libs/java-json.jar"))
    testImplementation("junit:junit:4.12")
}

configure<org.sonarqube.gradle.SonarQubeExtension> {
    properties {
        property("sonar.projectName", "Android")
        property("sonar.projectKey", "Android")
    }
}

tasks.create("mediaSdkJavadocs", Javadoc::class.java) {
    source(android.sourceSets["main"].java.srcDirs, "build/generated/source/buildConfig/release/")
    this.classpath = project.files(android.bootClasspath.joinToString(File.pathSeparator))
    title = "mParticle Android Media SDK API Reference"
    setFailOnError(false)
    (getOptions() as? StandardJavadocDocletOptions)?.noTimestamp(true)
}