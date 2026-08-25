package org.printscript.lexer

import org.printscript.token.Token

data class TokenMatch(val token: Token, val nextIndex: Int)
