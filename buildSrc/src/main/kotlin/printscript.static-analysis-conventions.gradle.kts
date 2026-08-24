// Analisis estatico: herramientas que VERIFICAN el codigo y hacen fallar el
// build. ktlint mira la forma (espacios, saltos, imports); detekt mira la
// estructura (complejidad, magic numbers, cantidad de returns).
//
// Las reglas propias viven fuera de este archivo, versionadas con el proyecto:
//   .editorconfig            -> ktlint (y tambien el IDE)
//   config/detekt/detekt.yml -> detekt
plugins {
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    // El YAML propio es un diff contra el default, no un reemplazo: sin esto,
    // toda regla que no escribamos queda apagada.
    buildUponDefaultConfig = true
}
