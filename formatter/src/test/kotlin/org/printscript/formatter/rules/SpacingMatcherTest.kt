package org.printscript.formatter.rules

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpacingMatcherTest {
    private val cualquiera = Token(TokenType.COLON, ":", Range(Position(1, 1), Position(1, 1)))

    private fun siempre(spacing: String) = SpacingRule { _, _ -> spacing }

    private val nunca = SpacingRule { _, _ -> null }

    @Test
    fun `sin reglas no contesta`() {
        assertNull(SpacingMatcher().spacingFor(null, cualquiera))
    }

    @Test
    fun `si ninguna regla opina tampoco contesta`() {
        assertNull(SpacingMatcher(listOf(nunca, nunca)).spacingFor(null, cualquiera))
    }

    @Test
    fun `contesta la primera que opina`() {
        val matcher = SpacingMatcher(listOf(nunca, siempre("  "), siempre(" ")))

        assertEquals("  ", matcher.spacingFor(null, cualquiera))
    }
}
