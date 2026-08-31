package org.printscript.formatter.expressions

import org.printscript.ast.BinaryOperator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrecedenceTest {
    @Test
    fun `sumar y restar comparten precedencia`() {
        assertEquals(Precedence.of(BinaryOperator.PLUS), Precedence.of(BinaryOperator.MINUS))
    }

    @Test
    fun `multiplicar y dividir comparten precedencia`() {
        assertEquals(Precedence.of(BinaryOperator.TIMES), Precedence.of(BinaryOperator.DIVIDE))
    }

    @Test
    fun `lo aditivo liga mas flojo que lo multiplicativo`() {
        assertTrue(Precedence.ADDITIVE.bindsLooserThan(Precedence.MULTIPLICATIVE))
        assertFalse(Precedence.MULTIPLICATIVE.bindsLooserThan(Precedence.ADDITIVE))
    }

    @Test
    fun `una precedencia nunca liga mas flojo que si misma`() {
        assertFalse(Precedence.ADDITIVE.bindsLooserThan(Precedence.ADDITIVE))
        assertFalse(Precedence.MULTIPLICATIVE.bindsLooserThan(Precedence.MULTIPLICATIVE))
    }

    @Test
    fun `una precedencia no liga mas fuerte que si misma`() {
        assertTrue(Precedence.ADDITIVE.bindsNoTighterThan(Precedence.ADDITIVE))
        assertTrue(Precedence.MULTIPLICATIVE.bindsNoTighterThan(Precedence.MULTIPLICATIVE))
        assertFalse(Precedence.MULTIPLICATIVE.bindsNoTighterThan(Precedence.ADDITIVE))
    }
}
