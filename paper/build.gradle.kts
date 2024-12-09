plugins {
    id("java")
}

group = "io.github.milkdrinkers"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
//    compileOnly(libs.annotations)
//    annotationProcessor(libs.annotations)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}