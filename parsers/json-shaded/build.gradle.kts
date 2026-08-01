import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.publisher)
    signing
}

val shade: Configuration by configurations.creating

dependencies {
    api(projects.common)

    shade(projects.parsers.json) {
        isTransitive = false
    }
    shade(libs.gson)
}

tasks.shadowJar {
    configurations = listOf(shade)
    archiveClassifier.set("")
    relocate("com.google.gson", "io.github.milkdrinkers.wordweaver.lib.gson")
    relocate("com.google.errorprone", "io.github.milkdrinkers.wordweaver.lib.errorprone")

    mergeServiceFiles()

    minimize {
        exclude(project(":parsers:json"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.milkdrinkers",
        artifactId = "wordweaver-json-shaded",
        version = version.toString().let { originalVersion ->
            if (!originalVersion.contains("-SNAPSHOT"))
                originalVersion
            else
                originalVersion.substringBeforeLast("-SNAPSHOT") + "-SNAPSHOT" // Force append just -SNAPSHOT if snapshot version
        }
    )

    pom {
        name.set("WordWeaver JSON (Shaded)")
        description.set("JSON/JSONC translation parser for WordWeaver with GSON shaded and relocated.")
        url.set("https://github.com/milkdrinkers/WordWeaver")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("darksaid98")
                name.set("darksaid98")
                url.set("https://github.com/darksaid98")
                organization.set("Milkdrinkers")
            }
        }

        scm {
            url.set("https://github.com/milkdrinkers/WordWeaver")
            connection.set("scm:git:git://github.com/milkdrinkers/WordWeaver.git")
            developerConnection.set("scm:git:ssh://github.com:milkdrinkers/WordWeaver.git")
        }
    }

    configure(JavaLibrary(
        javadocJar = JavadocJar.None(),
    ))

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}

signing {
    isRequired = false
}
