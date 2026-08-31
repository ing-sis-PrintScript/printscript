package org.printscript.formatter.expressions

import org.printscript.ast.BinaryOperator
import org.printscript.ast.UnaryOperator

internal fun UnaryOperator.symbol(): String =
    when (this) {
        UnaryOperator.MINUS -> "-"
    }

internal fun BinaryOperator.symbol(): String =
    when (this) {
        BinaryOperator.PLUS -> "+"
        BinaryOperator.MINUS -> "-"
        BinaryOperator.TIMES -> "*"
        BinaryOperator.DIVIDE -> "/"
    }
