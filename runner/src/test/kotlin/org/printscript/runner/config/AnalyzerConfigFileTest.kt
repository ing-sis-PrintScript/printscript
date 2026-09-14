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
                """
                identifier_format: "snake case"
                mandatory-variable-or-literal-in-println: false
                """.trimIndent(),
            ).getOrNull()

        assertEquals(AnalyzerConfig(SnakeCase, restrictPrintlnArguments = false), config)
    }

    @Test
    fun `las dos convenciones llevan un espacio en el medio`() {
        assertEquals(AnalyzerConfig(CamelCase), loadAnalyzerConfig("""identifier_format: "camel case" """).getOrNull())
        assertEquals(AnalyzerConfig(SnakeCase), loadAnalyzerConfig("""identifier_format: "snake case" """).getOrNull())
    }

    // Sin claves no hay reglas: es el caso valid-no-rules del TCK.
    @Test
    fun `un archivo vacio no prende ninguna regla`() {
        assertEquals(AnalyzerConfig(), loadAnalyzerConfig("").getOrNull())
    }

    @Test
    fun `una clave sola deja la otra en su default`() {
        val config = loadAnalyzerConfig("""identifier_format: "snake case" """).getOrNull()

        assertEquals(AnalyzerConfig(SnakeCase), config)
    }

    @Test
    fun `json y yaml equivalentes dan lo mismo`() {
        val json = loadAnalyzerConfig("""{ "identifier_format": "snake case" }""").getOrNull()
        val yaml = loadAnalyzerConfig("""identifier_format: "snake case" """).getOrNull()

        assertEquals(yaml, json)
    }

    @Test
    fun `una convencion que no existe es un error`() {
        val error = assertNotNull(loadAnalyzerConfig("identifier_format: PascalCase").errorOrNull())

        assertTrue(error.message.contains("camel case"))
    }

    // La regla de readInput es de 1.1 y todavia no existe, pero la clave ya llega en los
    // configs del TCK y el loader tiene que aceptarla.
    @Test
    fun `acepta la clave de readInput aunque la regla no exista todavia`() {
        val config = loadAnalyzerConfig("mandatory-variable-or-literal-in-readInput: true").getOrNull()

        assertEquals(AnalyzerConfig(restrictReadInputArguments = true), config)
    }

    @Test
    fun `una clave desconocida es un error, no se ignora`() {
        val error = assertNotNull(loadAnalyzerConfig("identifier_formatt: camel case").errorOrNull())

        assertTrue(error.message.contains("identifier_formatt"))
    }

    @Test
    fun `un valor del tipo equivocado es un error`() {
        val error = assertNotNull(loadAnalyzerConfig("mandatory-variable-or-literal-in-println: 2").errorOrNull())

        assertTrue(error.message.contains("true o false"))
    }
}
