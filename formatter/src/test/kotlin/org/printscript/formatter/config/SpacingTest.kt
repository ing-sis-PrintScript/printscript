package org.printscript.formatter.config

import kotlin.test.Test
import kotlin.test.assertEquals

class SpacingTest {
    @Test
    fun `NONE no renderiza ningun espacio`() {
        assertEquals("", Spacing.NONE.render())
    }

    @Test
    fun `SINGLE renderiza un unico espacio`() {
        assertEquals(" ", Spacing.SINGLE.render())
    }
}
