package org.printscript.formatter.expressions

import kotlin.test.Test
import kotlin.test.assertEquals

class RenderNumberTest {
    @Test
    fun `un valor entero no arrastra el punto decimal`() {
        assertEquals("5", renderNumber(5.0))
        assertEquals("0", renderNumber(0.0))
        assertEquals("-3", renderNumber(-3.0))
    }

    @Test
    fun `un valor decimal conserva sus decimales`() {
        assertEquals("0.5", renderNumber(0.5))
        assertEquals("12.5", renderNumber(12.5))
    }

    @Test
    fun `un decimal inexacto en binario no expande su representacion`() {
        assertEquals("0.1", renderNumber(0.1))
    }

    @Test
    fun `un valor grande no sale en notacion cientifica`() {
        assertEquals("100000000000000000000", renderNumber(1.0e20))
    }

    @Test
    fun `un valor no finito se renderiza en vez de romper`() {
        assertEquals("NaN", renderNumber(Double.NaN))
        assertEquals("Infinity", renderNumber(Double.POSITIVE_INFINITY))
        assertEquals("-Infinity", renderNumber(Double.NEGATIVE_INFINITY))
    }
}

class RenderStringTest {
    @Test
    fun `un texto sin comillas adentro va entre comillas dobles`() {
        assertEquals("\"hola\"", renderString("hola"))
    }

    @Test
    fun `un texto vacio va entre comillas dobles`() {
        assertEquals("\"\"", renderString(""))
    }

    @Test
    fun `un texto con comilla simple adentro va entre comillas dobles`() {
        assertEquals("\"it's\"", renderString("it's"))
    }

    @Test
    fun `un texto con comilla doble adentro va entre comillas simples`() {
        assertEquals("'dijo \"hola\"'", renderString("dijo \"hola\""))
    }

    @Test
    fun `un texto con los dos tipos de comilla cae en comillas dobles`() {
        assertEquals("\"it's \"x\"\"", renderString("it's \"x\""))
    }
}
