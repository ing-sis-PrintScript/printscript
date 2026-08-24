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
 * Tests del reconocedor solo: sin secuencias, sin streaming, sin lineas.
 * Le paso una linea y un indice, y verifico que unidad lexica sale.
 */
class TokenMatcherTest {

    private val matcher = TokenMatcher()

    private fun matchAt(line: String, from: Int) =
        assertNotNull(matcher.match(line, from, 1).getOrNull(), "esperaba un token")

    @Test
    fun `reconoce una keyword`() {
        val match = matchAt("let x = 5;", 0)
        assertEquals(TokenType.LET, match.token.type)
        assertEquals("let", match.token.value)
    }

    @Test
    fun `una palabra que no es keyword es identificador`() {
        assertEquals(TokenType.IDENTIFIER, matchAt("letter", 0).token.type)
    }

    @Test
    fun `reconoce un decimal completo`() {
        assertEquals("12.5", matchAt("12.5 / 4", 0).token.value)
    }

    /**
     * El valor de un string va sin comillas, pero el token igual OCUPO las dos
     * posiciones extra en el fuente. Por eso se verifican las tres cosas: sin
     * el range y el nextIndex, una regla que sacara bien las comillas pero
     * midiera mal quedaria desfasada un caracter en todo lo que viene despues.
     */
    @Test
    fun `el string guarda el contenido sin comillas pero ocupa las dos posiciones extra`() {
        val match = matchAt("""nombre = "Joe";""", 9)

        assertEquals(TokenType.STRING_LITERAL, match.token.type)
        assertEquals("Joe", match.token.value)
        assertEquals(Position(1, 10), match.token.range.start)
        assertEquals(Position(1, 14), match.token.range.end)
        assertEquals(14, match.nextIndex)
    }

    @Test
    fun `arranca a matchear desde el indice que le paso`() {
        val match = matchAt("let x = 5;", 8)
        assertEquals(TokenType.NUMBER_LITERAL, match.token.type)
        assertEquals("5", match.token.value)
    }

    /** El nextIndex de una unidad comun es el primer caracter despues de ella. */
    @Test
    fun `el nextIndex apunta al caracter siguiente al token`() {
        assertEquals(3, matchAt("let x = 5;", 0).nextIndex)
        assertEquals(5, matchAt("let x = 5;", 4).nextIndex)
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
        assertEquals(
            TokenType.IDENTIFIER,
            assertNotNull(sinKeywords.match("let x", 0, 1).getOrNull()).token.type,
        )
    }

    /** Una regla se puede testear sola, sin matcher ni lexer de por medio. */
    @Test
    fun `NumberRule contesta null si no arranca con digito`() {
        assertNull(NumberRule.match("let x", 0, 1))
        assertNotNull(NumberRule.match("42", 0, 1))
    }
}
