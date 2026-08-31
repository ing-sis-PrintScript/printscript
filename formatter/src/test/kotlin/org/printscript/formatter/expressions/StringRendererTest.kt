package org.printscript.formatter.expressions

import kotlin.test.Test
import kotlin.test.assertEquals

class StringRendererTest {
    private val renderer = StringRenderer()

    @Test
    fun `un texto sin comillas adentro va entre comillas dobles`() {
        assertEquals("\"hola\"", renderer.render("hola"))
    }

    @Test
    fun `un texto vacio va entre comillas dobles`() {
        assertEquals("\"\"", renderer.render(""))
    }

    @Test
    fun `un texto con comilla simple adentro va entre comillas dobles`() {
        assertEquals("\"it's\"", renderer.render("it's"))
    }

    @Test
    fun `un texto con comilla doble adentro va entre comillas simples`() {
        assertEquals("'dijo \"hola\"'", renderer.render("dijo \"hola\""))
    }

    @Test
    fun `un texto con los dos tipos de comilla cae en comillas dobles`() {
        assertEquals("\"it's \"x\"\"", renderer.render("it's \"x\""))
    }
}
