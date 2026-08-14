package org.printscript.token

import org.printscript.common.Range

data class Token(val type: TokenType, val lexeme: String, val value : String, val range: Range)