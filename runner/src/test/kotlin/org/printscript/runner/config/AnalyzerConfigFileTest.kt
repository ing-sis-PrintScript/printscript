package org.printscript.runner.config

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.CamelCase
import org.printscript.analyzer.config.SnakeCase
import org.printscript.common.errorOrNull
import org.printscript.common.getOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AnalyzerConfigFileTest {
    @Test
    fun `lee las dos reglas`() {
        val config =
            loadAnalyzerConfig(
                "reglas.yaml",
                """
                identifier-naming: snake-case
                println-only-simple-arguments: false
                """.trimIndent(),
            ).getOrNull()

        assertEquals(AnalyzerConfig(SnakeCase, restrictPrintlnArguments = false), config)
    }

    @Test
    fun `un archivo vacio deja los defaults`() {
        assertEquals(
            AnalyzerConfig(CamelCase, restrictPrintlnArguments = true),
            loadAnalyzerConfig("reglas.yaml", "").getOrNull(),
        )
    }

    @Test
    fun `una clave sola deja la otra en su default`() {
        val config = loadAnalyzerConfig("reglas.yaml", "identifier-naming: snake-case").getOrNull()

        assertEquals(AnalyzerConfig(SnakeCase, restrictPrintlnArguments = true), config)
    }

    @Test
    fun `json y yaml equivalentes dan lo mismo`() {
        val json = loadAnalyzerConfig("reglas.json", """{ "identifier-naming": "snake-case" }""").getOrNull()
        val yaml = loadAnalyzerConfig("reglas.yaml", "identifier-naming: snake-case").getOrNull()

        assertEquals(yaml, json)
    }

    @Test
    fun `una convencion que no existe es un error`() {
        val error = assertNotNull(loadAnalyzerConfig("reglas.yaml", "identifier-naming: PascalCase").errorOrNull())

        assertTrue(error.message.contains("camel-case"))
    }

    @Test
    fun `una clave desconocida es un error, no se ignora`() {
        val error = assertNotNull(loadAnalyzerConfig("reglas.yaml", "identifier-namming: camel-case").errorOrNull())

        assertTrue(error.message.contains("identifier-namming"))
    }

    @Test
    fun `un valor del tipo equivocado es un error`() {
        val error = assertNotNull(loadAnalyzerConfig("reglas.yaml", "println-only-simple-arguments: 2").errorOrNull())

        assertTrue(error.message.contains("true o false"))
    }
}
