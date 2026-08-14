plugins {
    `kotlin-dsl`
}

dependencies {
    // Habilita `kotlin("jvm")` sin version dentro de los convention plugins.
    implementation(libs.kotlin.gradle.plugin)
}
