package org.printscript.formatter

import org.printscript.common.Result
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals
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
                blankLinesBeforePrintln = BlankLines.ONE,
            )

        assertEquals("let x: number = 5;\n\nprintln(x);", formatear("let x:number=5;\nprintln(x);", config))
    }

    @Test
    fun `el factory arma las tres reglas y cada una responde por lo suyo`() {
        assertEquals(3, PrintScript10.rules(FormatterConfig()).size)
    }

    @Test
    fun `sin config no toca nada`() {
        val fuente = "let  x :number   =5;\nprintln( x );"

        assertEquals(fuente, formatear(fuente, FormatterConfig()))
    }
}
