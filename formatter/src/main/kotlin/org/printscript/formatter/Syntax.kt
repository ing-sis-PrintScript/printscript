package org.printscript.formatter

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
