package org.printscript.formatter.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

class BlankLinesTest {
    @Test
    fun `cada constante renderiza tantos saltos como representa`() {
        assertEquals("", BlankLines.NONE.render())
        assertEquals("\n", BlankLines.ONE.render())
        assertEquals("\n\n", BlankLines.TWO.render())
    }

    @Test
    fun `of resuelve cero uno y dos lineas en blanco`() {
        assertEquals(BlankLines.NONE, BlankLines.of(0))
        assertEquals(BlankLines.ONE, BlankLines.of(1))
        assertEquals(BlankLines.TWO, BlankLines.of(2))
    }

    @Test
    fun `of no resuelve una cantidad fuera del rango permitido`() {
        assertNull(BlankLines.of(3))
        assertNull(BlankLines.of(-1))
    }
}
