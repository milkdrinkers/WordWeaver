import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.publisher)
    signing
}

dependencies {
    api(projects.common)
    compileOnly(libs.gson)

    testImplementation(libs.gson)
    testImplementation(libs.slf4j.simple)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.milkdrinkers",
        artifactId = "wordweaver-json",
        version = version.toString().let { originalVersion ->
            if (!originalVersion.contains("-SNAPSHOT"))
                originalVersion
            else
                originalVersion.substringBeforeLast("-SNAPSHOT") + "-SNAPSHOT" // Force append just -SNAPSHOT if snapshot version
        }
    )

    pom {
        name.set("WordWeaver JSON")
        description.set("JSON/JSONC translation parser for WordWeaver. Requires a GSON dependency to be provided by the consumer.")
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
