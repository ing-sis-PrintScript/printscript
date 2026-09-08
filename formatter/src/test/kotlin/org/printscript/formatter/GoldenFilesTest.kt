package org.printscript.formatter

import org.printscript.common.Result
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GoldenFilesTest {
    // Se juntan las lineas con "\n", igual que SuiteOps.readFile del TCK: el salto final
    // del archivo no cuenta. El formatter tampoco lo emite.
    private fun recurso(path: String): String =
        checkNotNull(javaClass.getResourceAsStream(path)) { "falta el recurso $path" }
            .bufferedReader()
            .readLines()
            .joinToString("\n")

    private fun formatear(
        fuente: String,
        config: FormatterConfig,
    ): String =
        PrintScript10.formatter(config)
            .format(Lexer().tokenize(fuente))
            .joinToString("") { result ->
                when (result) {
                    is Result.Success -> result.value.text
                    is Result.Failure -> fail("no esperaba un error: ${result.error.message}")
                }
            }

    private fun verificar(
        fuente: String,
        golden: String,
        config: FormatterConfig,
    ) = assertEquals(recurso("/golden/$golden"), formatear(recurso("/source/$fuente"), config))

    // Las tres reglas de espaciado en su forma habitual, mas una linea en blanco antes
    // del println.
    private val canonico =
        FormatterConfig(
            spaceBeforeColon = Spacing.NONE,
            spaceAfterColon = Spacing.SINGLE,
            spaceAroundAssignment = Spacing.SINGLE,
            blankLinesBeforePrintln = BlankLines.ONE,
        )

    @Test
    fun `ejemplo 1`() {
        verificar("ejemplo1.ps", "ejemplo1.ps", canonico)
    }

    @Test
    fun `ejemplo 2`() {
        verificar("ejemplo2.ps", "ejemplo2.ps", canonico)
    }

    @Test
    fun `ejemplo 3 con una asignacion`() {
        verificar("ejemplo3.ps", "ejemplo3.ps", canonico)
    }

    @Test
    fun `ejemplo 1 con las tres reglas de espaciado apagadas`() {
        val config =
            canonico.copy(
                spaceBeforeColon = Spacing.NONE,
                spaceAfterColon = Spacing.NONE,
                spaceAroundAssignment = Spacing.NONE,
            )

        verificar("ejemplo1.ps", "sin-espacios.ps", config)
    }

    @Test
    fun `ejemplo 1 con espacio antes de los dos puntos`() {
        verificar("ejemplo1.ps", "espacio-antes-de-dos-puntos.ps", canonico.copy(spaceBeforeColon = Spacing.SINGLE))
    }

    // Sin ninguna regla activa el archivo sale igual, parentesis incluidos. El formatter
    // viejo necesitaba un Parenthesizer para esto porque el AST no los guarda; preservando
    // el fuente el problema no existe.
    @Test
    fun `los parentesis sobreviven al formateo`() {
        verificar("parentesis.ps", "parentesis.ps", FormatterConfig())
    }
}
