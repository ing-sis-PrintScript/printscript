package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.lexer.TokenMatch
import org.printscript.token.TokenType

/** Simbolos de un solo caracter: : ; = ( ) + - * / */
class SymbolRule(private val symbols: Map<Char, TokenType>) : TokenRule {

    override fun match(line: String, from: Int, lineNumber: Int): Result<TokenMatch, LexicalError>? {
        val type = symbols[line[from]] ?: return null
        return matchOf(type, line[from].toString(), lineNumber, from)
    }
}
