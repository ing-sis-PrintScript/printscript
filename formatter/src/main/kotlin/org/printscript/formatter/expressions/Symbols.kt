package org.printscript.formatter.expressions

import org.printscript.ast.BinaryOperator

internal fun BinaryOperator.symbol(): String =
    when (this) {
        BinaryOperator.PLUS -> "+"
        BinaryOperator.MINUS -> "-"
        BinaryOperator.TIMES -> "*"
        BinaryOperator.DIVIDE -> "/"
    }
