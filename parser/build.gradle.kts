plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":ast"))
    implementation(project(":token"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}