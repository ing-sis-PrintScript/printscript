package org.printscript.formatter.rules

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.formatter.config.Spacing
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ColonSpacingRuleTest {
    private fun token(
        type: TokenType,
        value: String,
    ) = Token(type, value, Range(Position(1, 1), Position(1, 1)))

    private val colon = token(TokenType.COLON, ":")
    private val tipo = token(TokenType.TYPE_STRING, "string")
    private val nombre = token(TokenType.IDENTIFIER, "x")

    @Test
    fun `sin config no opina de nada`() {
        val rule = ColonSpacingRule()

        assertNull(rule.spacingFor(nombre, colon))
        assertNull(rule.spacingFor(colon, tipo))
    }

    @Test
    fun `pide un espacio antes del colon`() {
        assertEquals(" ", ColonSpacingRule(before = Spacing.SINGLE).spacingFor(nombre, colon))
    }

    @Test
    fun `pide que no haya espacio antes del colon`() {
        assertEquals("", ColonSpacingRule(before = Spacing.NONE).spacingFor(nombre, colon))
    }

    @Test
    fun `el espacio despues del colon se decide mirando el token anterior`() {
        assertEquals(" ", ColonSpacingRule(after = Spacing.SINGLE).spacingFor(colon, tipo))
    }

    @Test
    fun `configurar el antes no toca el despues`() {
        val rule = ColonSpacingRule(before = Spacing.NONE)

        assertNull(rule.spacingFor(colon, tipo))
    }

    @Test
    fun `no opina de tokens que no tienen nada que ver con el colon`() {
        val rule = ColonSpacingRule(before = Spacing.SINGLE, after = Spacing.SINGLE)

        assertNull(rule.spacingFor(nombre, token(TokenType.ASSIGN, "=")))
    }
}
