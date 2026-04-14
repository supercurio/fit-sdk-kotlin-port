pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "fit-sdk-kotlin-port"

include(":fit")
project(":fit").projectDir = file("fit-sdk")

include(":activity-repair")
include(":fit-csv-tool")
