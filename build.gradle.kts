import org.gradle.testing.jacoco.plugins.JacocoCoverageReport

// Sin allprojects {} ni subprojects {}: la configuracion compartida vive en los
// convention plugins de buildSrc/src/main/kotlin/printscript.*-conventions.gradle.kts
// y cada modulo la aplica explicitamente en su bloque plugins {}.

plugins {
    base
    id("jacoco-report-aggregation")
}

repositories {
    mavenCentral()
}

// El coverage por modulo miente en un proyecto multi-modulo: los tests del lexer
// ejecutan Result y Range, que viven en common, pero esa ejecucion se registra en
// el .exec del lexer. Agregando los seis modulos, cada clase cuenta donde vive.
dependencies {
    jacocoAggregation(project(":common"))
    jacocoAggregation(project(":token"))
    jacocoAggregation(project(":ast"))
    jacocoAggregation(project(":lexer"))
    jacocoAggregation(project(":parser"))
    jacocoAggregation(project(":interpreter"))
}

// La raiz no es un proyecto Java, asi que el plugin no puede deducir de que test
// suite sacar los datos. Se lo decimos: la suite "test" de cada modulo.
reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testSuiteName = "test"
        }
    }
}

// El plugin de agregacion genera el reporte pero no verifica nada. Esta task
// aplica el umbral sobre los mismos datos agregados.
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
                minimum = "0.99".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn("codeCoverageVerification")
}