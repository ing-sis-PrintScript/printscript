package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.common.errorOrNull
import org.printscript.common.getOrNull
import org.printscript.lexer.rules.NumberRule
import org.printscript.lexer.rules.WordRule
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests del reconocedor solo: sin secuencias, sin streaming, sin líneas.
 * Le paso una línea y un índice, y verifico qué token sale.
 */
class TokenMatcherTest {

    private val matcher = TokenMatcher()

    private fun matchAt(line: String, from: Int) =
        assertNotNull(matcher.match(line, from, 1).getOrNull(), "esperaba un token")

    @Test
    fun `reconoce una keyword`() {
        val token = matchAt("let x = 5;", 0)
        assertEquals(TokenType.LET, token.type)
        assertEquals("let", token.lexeme)
    }

    @Test
    fun `una palabra que no es keyword es identificador`() {
        assertEquals(TokenType.IDENTIFIER, matchAt("letter", 0).type)
    }

    @Test
    fun `reconoce un decimal completo`() {
        assertEquals("12.5", matchAt("12.5 / 4", 0).lexeme)
    }

    @Test
    fun `el lexema del string incluye las comillas`() {
        assertEquals("\"Joe\"", matchAt("""nombre = "Joe";""", 9).lexeme)
    }

    @Test
    fun `arranca a matchear desde el indice que le paso`() {
        val token = matchAt("let x = 5;", 8)
        assertEquals(TokenType.NUMBER_LITERAL, token.type)
        assertEquals("5", token.lexeme)
    }

    @Test
    fun `un caracter desconocido devuelve Failure`() {
        val error = matcher.match("let a = @;", 8, 1).errorOrNull()
        assertNotNull(error)
        assertEquals("Caracter inesperado '@'", error.message)
        assertEquals(Position(1, 9), error.range.start)
    }

    /** Las reglas entran por constructor: cambiando la lista cambia el lenguaje. */
    @Test
    fun `se le puede cambiar el juego de reglas`() {
        val sinKeywords = TokenMatcher(listOf(WordRule(emptyMap())))
        assertEquals(TokenType.IDENTIFIER, assertNotNull(sinKeywords.match("let x", 0, 1).getOrNull()).type)
    }

    /** Una regla se puede testear sola, sin matcher ni lexer de por medio. */
    @Test
    fun `NumberRule contesta null si no arranca con digito`() {
        assertNull(NumberRule.match("let x", 0, 1))
        assertNotNull(NumberRule.match("42", 0, 1))
    }
}
