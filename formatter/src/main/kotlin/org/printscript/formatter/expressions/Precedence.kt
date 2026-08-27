package org.printscript.formatter.expressions

import org.printscript.ast.BinaryOperator

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
