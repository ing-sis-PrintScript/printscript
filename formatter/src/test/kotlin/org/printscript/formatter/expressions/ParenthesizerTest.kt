package org.printscript.formatter.expressions

import org.printscript.ast.BinaryOperator
import org.printscript.ast.Expression
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
