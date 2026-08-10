plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":common"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}