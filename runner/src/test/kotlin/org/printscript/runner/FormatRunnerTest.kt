package org.printscript.runner

import org.printscript.common.Version
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
    ) = FormatRunner(config, Version.V10)
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
    fun `las lineas en blanco DESPUES de un println salen de la config`() {
        val dosLineas = FormatterConfig(lineBreaksAfterPrintln = BlankLines.TWO)

        assertEquals(
            "println(x);\n\n\nprintln(y);",
            format("println(x);\nprintln(y);", dosLineas),
        )
    }

    // La regla no toca lo que viene despues de una declaracion, solo de un println.
    @Test
    fun `una declaracion seguida de println queda como estaba`() {
        val dosLineas = FormatterConfig(lineBreaksAfterPrintln = BlankLines.TWO)
        val fuente = "let x: number = 5;\nprintln(x);"

        assertEquals(fuente, format(fuente, dosLineas))
    }
}
