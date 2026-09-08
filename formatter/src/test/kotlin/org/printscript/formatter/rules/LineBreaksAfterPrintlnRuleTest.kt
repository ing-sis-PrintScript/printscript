package org.printscript.formatter.rules

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.formatter.config.BlankLines
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LineBreaksAfterPrintlnRuleTest {
    private fun token(
        type: TokenType,
        value: String,
    ) = Token(type, value, Range(Position(1, 1), Position(1, 1)))

    private val puntoYComa = token(TokenType.SEMICOLON, ";")
    private val siguiente = token(TokenType.PRINTLN, "println")
    private val vengoDePrintln = FormattingState(lastStatementHead = TokenType.PRINTLN)

    @Test
    fun `sin config no opina`() {
        assertNull(LineBreaksAfterPrintlnRule().spacingFor(puntoYComa, siguiente, vengoDePrintln))
    }

    @Test
    fun `cero lineas en blanco sigue dejando el salto que separa sentencias`() {
        val rule = LineBreaksAfterPrintlnRule(BlankLines.NONE)

        assertEquals("\n", rule.spacingFor(puntoYComa, siguiente, vengoDePrintln))
    }

    @Test
    fun `una linea en blanco son dos saltos`() {
        val rule = LineBreaksAfterPrintlnRule(BlankLines.ONE)

        assertEquals("\n\n", rule.spacingFor(puntoYComa, siguiente, vengoDePrintln))
    }

    @Test
    fun `no opina si la sentencia anterior no era un println`() {
        val rule = LineBreaksAfterPrintlnRule(BlankLines.ONE)

        assertNull(rule.spacingFor(puntoYComa, siguiente, FormattingState(TokenType.LET)))
    }

    @Test
    fun `no opina en el medio de una sentencia`() {
        val rule = LineBreaksAfterPrintlnRule(BlankLines.ONE)
        val abre = token(TokenType.LPAREN, "(")

        assertNull(rule.spacingFor(siguiente, abre, vengoDePrintln))
    }

    // Que el archivo termine o no con salto lo decide otra regla, no esta.
    @Test
    fun `no opina antes del fin de archivo`() {
        val rule = LineBreaksAfterPrintlnRule(BlankLines.ONE)

        assertNull(rule.spacingFor(puntoYComa, token(TokenType.EOF, ""), vengoDePrintln))
    }
}
