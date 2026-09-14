package org.printscript.formatter.rules

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.formatter.config.Spacing
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun token(
    type: TokenType,
    value: String,
) = Token(type, value, Range(Position(1, 1), Position(1, 1)))

private val colon = token(TokenType.COLON, ":")
private val tipo = token(TokenType.TYPE_STRING, "string")
private val nombre = token(TokenType.IDENTIFIER, "x")
private val igual = token(TokenType.ASSIGN, "=")
private val mas = token(TokenType.PLUS, "+")
private val sinEstado = FormattingState()

class SurroundingSpacingTest {
    private val soloColon = setOf(TokenType.COLON)

    @Test
    fun `sin config no opina de nada`() {
        val rule = SurroundingSpacing(soloColon)

        assertNull(rule.spacingFor(nombre, colon, sinEstado))
        assertNull(rule.spacingFor(colon, tipo, sinEstado))
    }

    @Test
    fun `el before decide el espacio delante del token`() {
        assertEquals(" ", SurroundingSpacing(soloColon, before = Spacing.SINGLE).spacingFor(nombre, colon, sinEstado))
        assertEquals("", SurroundingSpacing(soloColon, before = Spacing.NONE).spacingFor(nombre, colon, sinEstado))
    }

    // "El espacio despues del ':'" es en realidad el espacio antes del token siguiente.
    @Test
    fun `el after se decide mirando el token anterior`() {
        assertEquals(" ", SurroundingSpacing(soloColon, after = Spacing.SINGLE).spacingFor(colon, tipo, sinEstado))
    }

    @Test
    fun `configurar un lado no toca el otro`() {
        assertNull(SurroundingSpacing(soloColon, before = Spacing.NONE).spacingFor(colon, tipo, sinEstado))
    }

    @Test
    fun `no opina de tokens que no estan en el conjunto`() {
        val rule = SurroundingSpacing(soloColon, Spacing.SINGLE, Spacing.SINGLE)

        assertNull(rule.spacingFor(nombre, igual, sinEstado))
    }
}

class SpacingFactoriesTest {
    @Test
    fun `colonSpacing atiende los dos lados del colon`() {
        val rule = colonSpacing(Spacing.NONE, Spacing.SINGLE)

        assertEquals("", rule.spacingFor(nombre, colon, sinEstado))
        assertEquals(" ", rule.spacingFor(colon, tipo, sinEstado))
    }

    @Test
    fun `assignmentSpacing usa el mismo valor de los dos lados del igual`() {
        val rule = assignmentSpacing(Spacing.SINGLE)

        assertEquals(" ", rule.spacingFor(nombre, igual, sinEstado))
        assertEquals(" ", rule.spacingFor(igual, nombre, sinEstado))
    }

    @Test
    fun `operatorSpacing prendida pone un espacio de cada lado`() {
        val rule = operatorSpacing(mandatory = true)

        assertEquals(" ", rule.spacingFor(nombre, mas, sinEstado))
        assertEquals(" ", rule.spacingFor(mas, nombre, sinEstado))
    }

    @Test
    fun `operatorSpacing apagada no opina`() {
        val rule = operatorSpacing(mandatory = false)

        assertNull(rule.spacingFor(nombre, mas, sinEstado))
        assertNull(rule.spacingFor(mas, nombre, sinEstado))
    }

    @Test
    fun `operatorSpacing cubre los cuatro operadores`() {
        val rule = operatorSpacing(mandatory = true)

        for (op in listOf(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH)) {
            assertEquals(" ", rule.spacingFor(nombre, token(op, "?"), sinEstado), "fallo con $op")
        }
    }
}
