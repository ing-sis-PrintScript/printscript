package org.printscript.formatter.rules

import org.printscript.token.TokenType

data class FormattingState(
    val lastStatementHead: TokenType? = null,
    val blockDepth: Int = 0,
    val pendingWhitespace: String = "",
)
