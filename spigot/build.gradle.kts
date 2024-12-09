dependencies {
    compileOnly(libs.annotations)
    annotationProcessor(libs.annotations)
    implementation(project(":common"))

    compileOnly(libs.spigot.api)
}

tasks.test {
    useJUnitPlatform()
}