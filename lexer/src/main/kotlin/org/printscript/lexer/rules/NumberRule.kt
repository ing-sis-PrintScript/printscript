package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.token.Token
import org.printscript.token.TokenType

/** Enteros y decimales: 12, 3.5 */
object NumberRule : TokenRule {

    override fun match(line: String, from: Int, lineNumber: Int): Result<Token, LexicalError>? {
        if (!line[from].isDigit()) return null

        var i = from
        while (i < line.length && line[i].isDigit()) i++
        if (i < line.length && line[i] == '.') {
            i++
            while (i < line.length && line[i].isDigit()) i++
        }

        return tokenOf(TokenType.NUMBER_LITERAL, line.substring(from, i), lineNumber, from)
    }
}
