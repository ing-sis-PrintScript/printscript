package org.printscript.formatter.expressions

import org.printscript.ast.BinaryOperator
import org.printscript.ast.Expression
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

class ParenthesizerTest {
    private val parenthesizer = Parenthesizer()

    private fun needs(
        parent: BinaryOperator,
        child: Expression,
        side: OperandSide,
    ) = parenthesizer.needsParentheses(parent, child, side)

    private val sum = binary(BinaryOperator.PLUS, id("b"), id("c"))
    private val subtraction = binary(BinaryOperator.MINUS, id("b"), id("c"))
    private val product = binary(BinaryOperator.TIMES, id("b"), id("c"))

    @Test
    fun `un hijo que no es binario nunca lleva parentesis`() {
        assertFalse(needs(BinaryOperator.TIMES, number(1.0), OperandSide.LEFT))
        assertFalse(needs(BinaryOperator.TIMES, number(1.0), OperandSide.RIGHT))
        assertFalse(needs(BinaryOperator.TIMES, id("a"), OperandSide.LEFT))
        assertFalse(needs(BinaryOperator.TIMES, string("a"), OperandSide.RIGHT))
        assertFalse(needs(BinaryOperator.TIMES, call("f", id("a")), OperandSide.RIGHT))
    }

    @Test
    fun `a la izquierda con menor precedencia lleva parentesis`() {
        assertTrue(needs(BinaryOperator.TIMES, sum, OperandSide.LEFT))
    }

    @Test
    fun `a la izquierda con la misma precedencia no lleva parentesis`() {
        assertFalse(needs(BinaryOperator.PLUS, sum, OperandSide.LEFT))
    }

    @Test
    fun `a la izquierda con mayor precedencia no lleva parentesis`() {
        assertFalse(needs(BinaryOperator.PLUS, product, OperandSide.LEFT))
    }

    @Test
    fun `a la derecha con menor precedencia lleva parentesis`() {
        assertTrue(needs(BinaryOperator.TIMES, sum, OperandSide.RIGHT))
    }

    @Test
    fun `a la derecha con la misma precedencia tambien lleva parentesis`() {
        assertTrue(needs(BinaryOperator.MINUS, subtraction, OperandSide.RIGHT))
        assertTrue(needs(BinaryOperator.PLUS, sum, OperandSide.RIGHT))
    }

    @Test
    fun `a la derecha con mayor precedencia no lleva parentesis`() {
        assertFalse(needs(BinaryOperator.PLUS, product, OperandSide.RIGHT))
    }
}
