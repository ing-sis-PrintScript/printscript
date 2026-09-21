package org.printscript.formatter.rules

import org.printscript.token.Token
import org.printscript.token.TokenType

private val BOUNDARIES = setOf(TokenType.SEMICOLON, TokenType.LBRACE, TokenType.RBRACE)

internal fun startsStatement(
    prev: Token?,
    current: Token,
): Boolean =
    prev != null &&
        prev.type in BOUNDARIES &&
        current.type != TokenType.EOF &&
        current.type != TokenType.ELSE
