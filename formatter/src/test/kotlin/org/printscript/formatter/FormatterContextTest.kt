package org.printscript.formatter

import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import kotlin.test.Test
import kotlin.test.assertEquals

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
