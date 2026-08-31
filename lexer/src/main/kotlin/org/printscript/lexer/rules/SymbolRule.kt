package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.lexer.TokenMatch
import org.printscript.token.TokenType

class SymbolRule(private val symbols: Map<String, TokenType>) : TokenRule {
    private val maxLength = symbols.keys.maxOfOrNull { it.length } ?: 0

    override fun match(
        line: String,
        from: Int,
        lineNumber: Int,
    ): Result<TokenMatch, LexicalError>? {
        var length = minOf(maxLength, line.length - from)
        while (length > 0) {
            val candidate = line.substring(from, from + length)
            val type = symbols[candidate]
            if (type != null) {
                return matchOf(type, candidate, lineNumber, from)
            }
            length--
        }
        return null
    }
}
