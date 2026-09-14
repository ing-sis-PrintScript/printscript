package org.printscript.formatter.rules

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LineBreakAfterStatementRuleTest {
    private fun token(
        type: TokenType,
        value: String,
    ) = Token(type, value, Range(Position(1, 1), Position(1, 1)))

    private val puntoYComa = token(TokenType.SEMICOLON, ";")
    private val let = token(TokenType.LET, "let")
    private val sinEstado = FormattingState()

    @Test
    fun `apagada no opina`() {
        assertNull(LineBreakAfterStatementRule().spacingFor(puntoYComa, let, sinEstado))
    }

    @Test
    fun `prendida corta la linea despues del punto y coma`() {
        assertEquals("\n", LineBreakAfterStatementRule(mandatory = true).spacingFor(puntoYComa, let, sinEstado))
    }

    @Test
    fun `no opina en el medio de una sentencia`() {
        val rule = LineBreakAfterStatementRule(mandatory = true)

        assertNull(rule.spacingFor(let, token(TokenType.IDENTIFIER, "x"), sinEstado))
    }

    // Que el archivo termine o no con salto lo decide otra cosa.
    @Test
    fun `no opina antes del fin de archivo`() {
        val rule = LineBreakAfterStatementRule(mandatory = true)

        assertNull(rule.spacingFor(puntoYComa, token(TokenType.EOF, ""), sinEstado))
    }
}
