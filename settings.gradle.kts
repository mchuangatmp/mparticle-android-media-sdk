include("media")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.library") version "7.1.3"
        id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
        kotlin("android") version "1.6.20"
        id("org.sonarqube") version "3.3"
        id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    }
}
