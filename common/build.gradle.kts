dependencies {
    compileOnly(libs.annotations)
    annotationProcessor(libs.annotations)

    implementation(libs.gson)

    testImplementation(libs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
}

tasks.test {
    useJUnitPlatform()
}