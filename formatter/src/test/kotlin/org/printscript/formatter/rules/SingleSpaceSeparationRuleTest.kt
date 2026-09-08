package org.printscript.formatter.rules

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.token.Token
import org.printscript.token.TokenType
import org.printscript.token.Trivia
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SingleSpaceSeparationRuleTest {
    private fun token(
        type: TokenType,
        value: String,
        trivia: String = "",
    ) = Token(type, value, Range(Position(1, 1), Position(1, 1)), Trivia(trivia))

    private val rule = SingleSpaceSeparationRule(mandatory = true)
    private val let = token(TokenType.LET, "let")
    private val sinEstado = FormattingState()

    @Test
    fun `apagada no opina`() {
        val apagada = SingleSpaceSeparationRule()

        assertNull(apagada.spacingFor(let, token(TokenType.IDENTIFIER, "x"), sinEstado))
    }

    @Test
    fun `separa dos tokens cualesquiera con un espacio`() {
        assertEquals(" ", rule.spacingFor(let, token(TokenType.IDENTIFIER, "x", "      "), sinEstado))
        assertEquals(" ", rule.spacingFor(token(TokenType.PRINTLN, "println"), token(TokenType.LPAREN, "("), sinEstado))
    }

    @Test
    fun `el punto y coma no lleva espacio adelante`() {
        assertEquals("", rule.spacingFor(token(TokenType.RPAREN, ")"), token(TokenType.SEMICOLON, ";", " "), sinEstado))
    }

    // Si el fuente cortaba la linea, el salto manda: los statements no se juntan.
    @Test
    fun `no pisa un salto de linea`() {
        val enOtraLinea = token(TokenType.PRINTLN, "println", "\n")

        assertNull(rule.spacingFor(token(TokenType.SEMICOLON, ";"), enOtraLinea, sinEstado))
    }

    @Test
    fun `no opina en el primer token del archivo`() {
        assertNull(rule.spacingFor(null, let, sinEstado))
    }
}
