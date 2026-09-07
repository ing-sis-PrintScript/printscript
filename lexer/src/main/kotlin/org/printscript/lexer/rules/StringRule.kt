package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.lexer.TokenMatch
import org.printscript.token.TokenType

object StringRule : TokenRule {
    override fun match(
        line: String,
        from: Int,
        lineNumber: Int,
    ): Result<TokenMatch, LexicalError>? {
        val quote = line[from]
        if (quote != '"' && quote != '\'') return null

        val closing = line.indexOf(quote, from + 1)
        if (closing == -1) {
            return errorOf("String sin cerrar", lineNumber, from, line.length - from)
        }

        return matchOf(
            TokenType.STRING_LITERAL,
            line.substring(from + 1, closing),
            closing - from + 1,
            lineNumber,
            from,
        )
    }
}
