package org.printscript.lexer.source

import org.printscript.common.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SourceCursorTest {
    private fun cursorOf(text: String) = SourceCursor.from(StringSourceReader(text))

    private fun foundOf(result: ScanResult): SourceCursor {
        assertIs<ScanResult.Found>(result, "esperaba encontrar un token y vino $result")
        return result.cursor
    }

    private tailrec fun agotar(cursor: SourceCursor): ScanResult.Exhausted =
        when (val result = cursor.moveToNextToken()) {
            is ScanResult.Found -> agotar(result.cursor.advanceTo(result.cursor.line.length))
            is ScanResult.Exhausted -> result
        }

    @Test
    fun `un fuente vacio no tiene tokens y termina en 1 1`() {
        assertEquals(ScanResult.Exhausted(Position(1, 1)), cursorOf("").moveToNextToken())
    }

    @Test
    fun `una linea de solo espacios tampoco tiene tokens`() {
        assertIs<ScanResult.Exhausted>(cursorOf("   ").moveToNextToken())
    }

    @Test
    fun `se para en el primer caracter significativo`() {
        val cursor = foundOf(cursorOf("   let x;").moveToNextToken())

        assertEquals(1, cursor.lineNumber)
        assertEquals(3, cursor.index)
        assertEquals('l', cursor.line[cursor.index])
    }

    @Test
    fun `el fin del fuente queda una columna despues del ultimo caracter`() {
        assertEquals(Position(1, 7), agotar(cursorOf("let x;")).endPosition)
    }

    @Test
    fun `cuenta las lineas y reinicia el indice en cada una`() {
        val primera = foundOf(cursorOf("a\nb").moveToNextToken())

        assertEquals(1, primera.lineNumber)
        assertEquals(0, primera.index)

        val segunda = foundOf(primera.advanceTo(1).moveToNextToken())

        assertEquals(2, segunda.lineNumber)
        assertEquals(0, segunda.index)
    }

    @Test
    fun `saltea lineas enteras de whitespace sin perder la cuenta`() {
        val cursor = foundOf(cursorOf("   \n\t\nx").moveToNextToken())

        assertEquals(3, cursor.lineNumber)
        assertEquals(0, cursor.index)
    }

    @Test
    fun `advanceTo mueve el cursor a donde indico la regla`() {
        val cursor = foundOf(cursorOf("let x;").moveToNextToken())

        assertEquals(4, foundOf(cursor.advanceTo(3).moveToNextToken()).index)
    }

    @Test
    fun `moveToNextToken no toca el cursor original`() {
        val cursor = cursorOf("   let x;")

        assertEquals(cursor.moveToNextToken(), cursor.moveToNextToken())
    }

    @Test
    fun `avanzar desde un cursor guardado da siempre el mismo resultado`() {
        val guardado = foundOf(cursorOf("let x;\nlet y;").moveToNextToken())

        val primero = guardado.advanceTo(guardado.line.length).moveToNextToken()
        agotar(guardado)

        assertEquals(primero, guardado.advanceTo(guardado.line.length).moveToNextToken())
    }

    @Test
    fun `no lee mas lineas de las que necesita`() {
        val leidas = ContadorDeLineas()

        SourceCursor.from(FuenteInfinita("x", leidas)).moveToNextToken()

        assertEquals(1, leidas.total())
    }
}
