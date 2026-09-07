package org.printscript.formatter

import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import kotlin.test.Test
import kotlin.test.assertEquals

class FormattedCodeTest {
    @Test
    fun `plus concatena el texto de los dos fragmentos`() {
        val result = FormattedCode("let a") + FormattedCode(" = 1;")

        assertEquals(FormattedCode("let a = 1;"), result)
    }

    @Test
    fun `EMPTY es neutro a izquierda y a derecha`() {
        val code = FormattedCode("println(a);")

        assertEquals(code, FormattedCode.EMPTY + code)
        assertEquals(code, code + FormattedCode.EMPTY)
    }
}

class FormatterContextTest {
    @Test
    fun `expone la config que se le paso`() {
        val config =
            FormatterConfig(
                spaceBeforeColon = Spacing.SINGLE,
                blankLinesBeforePrintln = BlankLines.TWO,
            )

        assertEquals(config, FormatterContext(config).config)
    }
}
