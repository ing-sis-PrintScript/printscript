package org.printscript.formatter

import org.printscript.common.Result
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.fail

class PrintScript10Test {
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

    @Test
    fun `el factory arma un formatter que formatea de punta a punta`() {
        val config =
            FormatterConfig(
                spaceBeforeColon = Spacing.NONE,
                spaceAfterColon = Spacing.SINGLE,
                spaceAroundAssignment = Spacing.SINGLE,
            )

        assertEquals("let x: number = 5;\n\nprintln(x);", formatear("let x:number=5;\n\nprintln(x);", config))
    }

    // Contar reglas no prueba nada. Lo que importa es que ninguna clave del config quede
    // sin cablear: si se agrega un campo y se olvida de sumarlo a PrintScript10.rules,
    // esa config no cambia nada y este test lo agarra.
    @Test
    fun `cada clave del config esta cableada a una regla`() {
        val fuente = "let a:number=1+2;println(a);println(a);"
        val configs =
            listOf(
                FormatterConfig(spaceBeforeColon = Spacing.SINGLE),
                FormatterConfig(spaceAfterColon = Spacing.SINGLE),
                FormatterConfig(spaceAroundAssignment = Spacing.SINGLE),
                FormatterConfig(lineBreaksAfterPrintln = BlankLines.ONE),
                FormatterConfig(lineBreakAfterStatement = true),
                FormatterConfig(spaceSurroundingOperations = true),
                FormatterConfig(singleSpaceSeparation = true),
            )

        for (config in configs) {
            assertNotEquals(fuente, formatear(fuente, config), "esta config no cambio nada: $config")
        }
    }

    @Test
    fun `sin config no toca nada`() {
        val fuente = "let  x :number   =5;\nprintln( x );"

        assertEquals(fuente, formatear(fuente, FormatterConfig()))
    }
}
