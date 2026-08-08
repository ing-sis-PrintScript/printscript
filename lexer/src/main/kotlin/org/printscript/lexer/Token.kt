package org.printscript.lexer

import org.printscript.common.Range

data class Token(val type: TokenType, val lexeme: String, val range: Range)

class LexicalError(message: String, val range: Range) : Exception(message)