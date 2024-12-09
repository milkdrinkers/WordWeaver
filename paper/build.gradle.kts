dependencies {
    compileOnly(libs.annotations)
    annotationProcessor(libs.annotations)
    implementation(project(":common"))

    compileOnly(libs.paper.api)
}

tasks.test {
    useJUnitPlatform()
}