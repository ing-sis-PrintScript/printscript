import org.gradle.testing.jacoco.plugins.JacocoCoverageReport

// El coverage por modulo miente en un proyecto multi-modulo: los tests del lexer
// ejecutan Result y Range, que viven en common, pero esa ejecucion se registra
// en el .exec del lexer. Medido por modulo common daba 0%; agregado, 78%.
plugins {
    base
    id("jacoco-report-aggregation")
}

// La raiz no aplica los convention plugins de los modulos, pero la task
// agregadora resuelve dependencias aca y necesita saber de donde bajarlas.
repositories {
    mavenCentral()
}

dependencies {
    // Todos los modulos entran, automaticamente: asi agregar formatter, linter
    // o cli no depende de acordarse de sumarlos a una lista.
    subprojects.forEach { jacocoAggregation(it) }
}

// La raiz no es un proyecto Java, asi que el plugin no puede deducir de que
// test suite sacar los datos. Se lo decimos.
reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testSuiteName = "test"
        }
    }
}

// El plugin de agregacion genera el reporte pero no verifica nada.
val coverageReport = tasks.named<JacocoReport>("testCodeCoverageReport")

tasks.register<JacocoCoverageVerification>("codeCoverageVerification") {
    dependsOn(coverageReport)
    executionData.setFrom(coverageReport.get().executionData)
    classDirectories.setFrom(coverageReport.get().classDirectories)
    sourceDirectories.setFrom(coverageReport.get().sourceDirectories)

    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("codeCoverageVerification")
}
