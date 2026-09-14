package org.printscript.formatter.rules

import org.printscript.token.Token
import org.printscript.token.TokenType

internal fun startsStatementAfterSemicolon(
    prev: Token?,
    current: Token,
): Boolean = prev?.type == TokenType.SEMICOLON && current.type != TokenType.EOF
