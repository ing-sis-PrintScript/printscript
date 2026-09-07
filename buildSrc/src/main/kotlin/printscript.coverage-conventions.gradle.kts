// Coverage por modulo. A diferencia del analisis estatico, esto no verifica
// nada: MIDE. El umbral se aplica sobre el agregado del proyecto entero, en
// printscript.coverage-aggregation.
plugins {
    jacoco
}

// El reporte se genera solo despues de correr los tests, para que nadie tenga
// que acordarse de pedirlo.
tasks.withType<Test>().configureEach {
    finalizedBy("jacocoTestReport")
}
