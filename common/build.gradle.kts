import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    alias(libs.plugins.publisher)
    signing
}

dependencies {
    implementation(libs.slf4j.api)
    compileOnlyApi(libs.adventure)

    testImplementation(libs.adventure)
    testImplementation(libs.slf4j.simple)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.milkdrinkers",
        artifactId = project.rootProject.name.lowercase(),
        version = version.toString().let { originalVersion ->
            if (!originalVersion.contains("-SNAPSHOT"))
                originalVersion
            else
                originalVersion.substringBeforeLast("-SNAPSHOT") + "-SNAPSHOT" // Force append just -SNAPSHOT if snapshot version
        }
    )

    pom {
        name.set(rootProject.name.capitalized())
        description.set(rootProject.description.orEmpty())
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
        javadocJar = JavadocJar.None(), // We want to use our own javadoc jar
    ))

    // Publish to Maven Central
    publishToMavenCentral(automaticRelease = true)

    // Sign all publications
    signAllPublications()
}

signing {
    isRequired = false // Skip signing if no credentials are provided, e.g. for local publishing
}
