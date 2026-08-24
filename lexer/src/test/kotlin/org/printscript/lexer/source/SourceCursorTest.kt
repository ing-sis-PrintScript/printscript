package org.printscript.lexer.source

import org.printscript.common.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SourceCursorTest {
    private fun cursorOf(vararg lines: String) = SourceCursor(lines.asSequence())

    @Test
    fun `un fuente vacio no tiene tokens y termina en 1 1`() {
        val cursor = cursorOf()

        assertFalse(cursor.moveToNextToken())
        assertEquals(Position(1, 1), cursor.endPosition())
    }

    @Test
    fun `una linea vacia tampoco tiene tokens`() {
        val cursor = cursorOf("")

        assertFalse(cursor.moveToNextToken())
        assertEquals(Position(1, 1), cursor.endPosition())
    }

    @Test
    fun `se para en el primer caracter significativo`() {
        val cursor = cursorOf("   let x;")

        assertTrue(cursor.moveToNextToken())
        assertEquals(1, cursor.lineNumber)
        assertEquals(3, cursor.index)
        assertEquals('l', cursor.line[cursor.index])
    }

    @Test
    fun `el fin del fuente queda una columna despues del ultimo caracter`() {
        val cursor = cursorOf("let x;")

        while (cursor.moveToNextToken()) cursor.advanceTo(cursor.line.length)

        assertEquals(Position(1, 7), cursor.endPosition())
    }

    @Test
    fun `cuenta las lineas y reinicia el indice en cada una`() {
        val cursor = cursorOf("a", "b")

        assertTrue(cursor.moveToNextToken())
        assertEquals(1, cursor.lineNumber)
        assertEquals(0, cursor.index)

        cursor.advanceTo(1)

        assertTrue(cursor.moveToNextToken())
        assertEquals(2, cursor.lineNumber)
        assertEquals(0, cursor.index)
    }

    @Test
    fun `saltea lineas enteras de whitespace sin perder la cuenta`() {
        val cursor = cursorOf("   ", "\t\t", "x")

        assertTrue(cursor.moveToNextToken())
        assertEquals(3, cursor.lineNumber)
        assertEquals(0, cursor.index)
    }

    @Test
    fun `advanceTo mueve el cursor a donde indico la regla`() {
        val cursor = cursorOf("let x;")

        cursor.moveToNextToken()
        cursor.advanceTo(3)

        assertTrue(cursor.moveToNextToken())
        assertEquals(4, cursor.index)
    }

    @Test
    fun `no lee mas lineas de las que necesita`() {
        var leidas = 0
        val infinitas =
            sequence {
                while (true) {
                    leidas++
                    yield("x")
                }
            }

        val cursor = SourceCursor(infinitas)
        cursor.moveToNextToken()

        assertEquals(1, leidas)
    }
}
