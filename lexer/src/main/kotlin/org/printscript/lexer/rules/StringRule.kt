package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.token.Token
import org.printscript.token.TokenType

/** Strings con comillas simples o dobles. Tienen que cerrar en la misma línea. */
object StringRule : TokenRule {

    override fun match(line: String, from: Int, lineNumber: Int): Result<Token, LexicalError>? {
        val quote = line[from]
        if (quote != '"' && quote != '\'') return null

        val closing = line.indexOf(quote, from + 1)
        if (closing == -1) {
            return errorOf("String sin cerrar", lineNumber, from, line.length - from)
        }

        return tokenOf(TokenType.STRING_LITERAL, line.substring(from, closing + 1), line.substring(from + 1, closing), lineNumber, from)
    }
}
