plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":common"))
    api(project(":token"))
    testImplementation(kotlin("test"))
}

// sin esto los tests de kotlin("test") no corren
tasks.test {
    useJUnitPlatform()
}
