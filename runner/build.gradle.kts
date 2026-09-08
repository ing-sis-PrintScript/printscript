plugins {
    id("printscript.published-library-conventions")
}

dependencies {
    api(project(":common"))
    api(project(":lexer"))
    api(project(":parser"))
    api(project(":interpreter"))
    api(project(":formatter"))
    api(project(":analyzer"))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.yaml)
}

// El TCK corre con 7 MB de heap. Los tests de memoria necesitan esa restriccion
// para probar algo; los demas no, y con 7 MB podrian fallar por motivos que no
// tienen que ver con lo que miden. Por eso van en su propia task.
tasks.named<Test>("test") {
    filter { excludeTestsMatching("*MemoryTest") }
}

val memoryTest by tasks.registering(Test::class) {
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter { includeTestsMatching("*MemoryTest") }
    maxHeapSize = "7m"
    useJUnitPlatform()
}

tasks.named("check") { dependsOn(memoryTest) }
