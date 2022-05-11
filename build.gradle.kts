plugins {
    id("org.sonarqube") apply true
    id("org.jlleitschuh.gradle.ktlint") apply true
}

allprojects {
    group = project.properties["group"].toString()
    version = project.properties["version"].toString()
    if (project.hasProperty("isRelease")) {
        version = version.toString().replace("-SNAPSHOT", "")
    }
    repositories {
        google()
        mavenCentral()
    }
}
