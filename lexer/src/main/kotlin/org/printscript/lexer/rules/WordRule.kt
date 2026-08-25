package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.lexer.TokenMatch
import org.printscript.token.TokenType

data class WordRule(private val keywords: Map<String, TokenType>) : TokenRule {
    override fun match(
        line: String,
        from: Int,
        lineNumber: Int,
    ): Result<TokenMatch, LexicalError>? {
        val first = line[from]
        if (!first.isLetter() && first != '_') return null

        var i = from
        while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) i++
        val text = line.substring(from, i)

        return matchOf(keywords[text] ?: TokenType.IDENTIFIER, text, lineNumber, from)
    }
}
