package org.printscript.runner

import org.printscript.common.getOrNull
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.lexer.source.StringSourceReader
import org.printscript.runner.progress.Progress
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatRunnerTest {
    private fun format(
        source: String,
        config: FormatterConfig = FormatterConfig(),
    ) = FormatRunner(config)
        .format(StringSourceReader(source))
        .mapNotNull { it.getOrNull() }
        .joinToString("") { it.text }

    @Test
    fun `normaliza los espacios de una declaracion`() {
        assertEquals(
            "let x: number = 5;\n",
            format("""let    x   :number=5;"""),
        )
    }

    @Test
    fun `la config cambia la salida`() {
        val sinEspacios = FormatterConfig(spaceAroundAssignment = Spacing.NONE)

        assertEquals(
            "let x: number=5;\n",
            format("""let x: number = 5;""", sinEspacios),
        )
    }

    @Test
    fun `los saltos antes de println salen de la config`() {
        val dosSaltos = FormatterConfig(blankLinesBeforePrintln = BlankLines.TWO)
        val salida =
            format(
                """
                let x: number = 5;
                println(x);
                """.trimIndent(),
                dosSaltos,
            )

        assertEquals("let x: number = 5;\n\n\nprintln(x);\n", salida)
    }

    @Test
    fun `avisa una vez por cada sentencia parseada`() {
        var notices = 0

        // El format() devuelve una Sequence perezosa: si nadie la consume no se
        // parsea nada, y el contador quedaria en cero.
        FormatRunner(FormatterConfig(), Progress { notices++ })
            .format(StringSourceReader("let x: number = 1;\nlet y: number = 2;"))
            .count()

        assertEquals(2, notices)
    }
}
