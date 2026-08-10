package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.token.Token
import org.printscript.token.TokenType

/**
 * Palabras: let, println, number, string, y cualquier identificador.
 *
 * Lee la palabra COMPLETA y recién ahí la busca en el mapa. Por eso "letter"
 * sale IDENTIFIER y no LET + ter.
 */
class WordRule(private val keywords: Map<String, TokenType>) : TokenRule {

    override fun match(line: String, from: Int, lineNumber: Int): Result<Token, LexicalError>? {
        val first = line[from]
        if (!first.isLetter() && first != '_') return null

        var i = from
        while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) i++
        val text = line.substring(from, i)

        return tokenOf(keywords[text] ?: TokenType.IDENTIFIER, text, lineNumber, from)
    }
}
