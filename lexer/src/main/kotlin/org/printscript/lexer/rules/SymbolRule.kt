package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.token.Token
import org.printscript.token.TokenType

/** Símbolos de un solo caracter: : ; = ( ) + - * / */
class SymbolRule(private val symbols: Map<Char, TokenType>) : TokenRule {

    override fun match(line: String, from: Int, lineNumber: Int): Result<Token, LexicalError>? {
        val type = symbols[line[from]] ?: return null
        return tokenOf(type, line[from].toString(), lineNumber, from)
    }
}
