include("media")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.library") version "7.3.1"
        id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
        kotlin("android") version "1.7.20"
        id("org.sonarqube") version "3.4.0.2513"
    }
}
