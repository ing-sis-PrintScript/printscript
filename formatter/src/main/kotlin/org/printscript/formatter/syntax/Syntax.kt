package org.printscript.formatter.syntax

import org.printscript.ast.BinaryOperator
import org.printscript.ast.DeclaredType
import org.printscript.ast.UnaryOperator
import org.printscript.formatter.config.Spacing

internal object Syntax {
    const val DECLARATION_KEYWORD = "let"
    const val PRINTLN = "println"
    const val TYPE_SEPARATOR = ":"
    const val ASSIGN = "="
    const val TERMINATOR = ";"
    const val OPEN_PAREN = "("
    const val CLOSE_PAREN = ")"
    const val ARGUMENT_SEPARATOR = ", "
    const val STATEMENT_SEPARATOR = "\n"

    val OPERATOR_SPACING: Spacing = Spacing.SINGLE
    val KEYWORD_SPACING: Spacing = Spacing.SINGLE
}

internal fun BinaryOperator.symbol(): String =
    when (this) {
        BinaryOperator.PLUS -> "+"
        BinaryOperator.MINUS -> "-"
        BinaryOperator.TIMES -> "*"
        BinaryOperator.DIVIDE -> "/"
    }

internal fun UnaryOperator.symbol(): String =
    when (this) {
        UnaryOperator.MINUS -> "-"
    }

internal fun DeclaredType.symbol(): String =
    when (this) {
        DeclaredType.NUMBER -> "number"
        DeclaredType.STRING -> "string"
    }
