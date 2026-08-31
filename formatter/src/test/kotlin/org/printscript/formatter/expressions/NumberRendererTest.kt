package org.printscript.formatter.expressions

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberRendererTest {
    private val renderer = NumberRenderer()

    @Test
    fun `un valor entero no arrastra el punto decimal`() {
        assertEquals("5", renderer.render(5.0))
        assertEquals("0", renderer.render(0.0))
        assertEquals("-3", renderer.render(-3.0))
    }

    @Test
    fun `un valor decimal conserva sus decimales`() {
        assertEquals("0.5", renderer.render(0.5))
        assertEquals("12.5", renderer.render(12.5))
    }

    @Test
    fun `un decimal inexacto en binario no expande su representacion`() {
        assertEquals("0.1", renderer.render(0.1))
    }

    @Test
    fun `un valor grande no sale en notacion cientifica`() {
        assertEquals("100000000000000000000", renderer.render(1.0e20))
    }

    @Test
    fun `un valor no finito se renderiza en vez de romper`() {
        assertEquals("NaN", renderer.render(Double.NaN))
        assertEquals("Infinity", renderer.render(Double.POSITIVE_INFINITY))
        assertEquals("-Infinity", renderer.render(Double.NEGATIVE_INFINITY))
    }
}
