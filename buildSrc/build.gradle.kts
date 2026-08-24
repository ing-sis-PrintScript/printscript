plugins {
    `kotlin-dsl`
}

dependencies {
    // Habilita `kotlin("jvm")` sin version dentro de los convention plugins.
    implementation(libs.kotlin.gradle.plugin)
    // Idem para id("org.jlleitschuh.gradle.ktlint").
    implementation(libs.ktlint.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
}
