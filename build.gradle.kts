plugins {
    `java-library`
}

subprojects {
    apply(plugin = "java-library")

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        mavenCentral()
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}