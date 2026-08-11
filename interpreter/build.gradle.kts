plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":common"))
    api(project(":ast"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}