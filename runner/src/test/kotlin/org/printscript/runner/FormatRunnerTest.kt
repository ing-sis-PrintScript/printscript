package org.printscript.runner

import org.printscript.common.getOrNull
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.lexer.source.StringSourceReader
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatRunnerTest {
    private fun format(
        source: String,
        config: FormatterConfig = FormatterConfig(),
    ) = FormatRunner(config)
        .format { StringSourceReader(source) }
        .mapNotNull { it.getOrNull() }
        .joinToString("") { it.text }

    @Test
    fun `sin config devuelve el fuente tal cual`() {
        val fuente = "let    x   :number=5;"

        assertEquals(fuente, format(fuente))
    }

    @Test
    fun `acomoda el espaciado de la declaracion donde la config manda`() {
        val config =
            FormatterConfig(
                spaceBeforeColon = Spacing.NONE,
                spaceAfterColon = Spacing.SINGLE,
                spaceAroundAssignment = Spacing.SINGLE,
            )

        assertEquals("let x: number = 5;", format("let x   :number=5;", config))
    }

    @Test
    fun `la config cambia la salida`() {
        val sinEspacios = FormatterConfig(spaceAroundAssignment = Spacing.NONE)

        assertEquals("let x: number=5;", format("let x: number = 5;", sinEspacios))
    }

    @Test
    fun `los saltos antes de println salen de la config`() {
        val dosSaltos = FormatterConfig(blankLinesBeforePrintln = BlankLines.TWO)

        assertEquals(
            "let x: number = 5;\n\n\nprintln(x);",
            format("let x: number = 5;\nprintln(x);", dosSaltos),
        )
    }
}
