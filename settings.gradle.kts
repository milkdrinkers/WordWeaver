pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" // allow automatic download of JDKs
}

rootProject.name = "WordWeaver"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "common",
)