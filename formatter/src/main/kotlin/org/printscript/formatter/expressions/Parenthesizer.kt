package org.printscript.formatter.expressions

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.Expression

enum class OperandSide { LEFT, RIGHT }

internal enum class Precedence {
    ADDITIVE,
    MULTIPLICATIVE,
    ;

    fun bindsLooserThan(other: Precedence): Boolean = this < other

    fun bindsNoTighterThan(other: Precedence): Boolean = this <= other

    companion object {
        fun of(operator: BinaryOperator): Precedence =
            when (operator) {
                BinaryOperator.PLUS, BinaryOperator.MINUS -> ADDITIVE
                BinaryOperator.TIMES, BinaryOperator.DIVIDE -> MULTIPLICATIVE
            }
    }
}

class Parenthesizer {
    fun needsParentheses(
        parent: BinaryOperator,
        child: Expression,
        side: OperandSide,
    ): Boolean = child is BinaryExpression && side.wraps(Precedence.of(child.operator), Precedence.of(parent))

    private fun OperandSide.wraps(
        child: Precedence,
        parent: Precedence,
    ): Boolean =
        when (this) {
            OperandSide.LEFT -> child.bindsLooserThan(parent)
            OperandSide.RIGHT -> child.bindsNoTighterThan(parent)
        }
}
