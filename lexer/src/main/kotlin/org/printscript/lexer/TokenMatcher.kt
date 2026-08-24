package org.printscript.lexer

import org.printscript.common.Result
import org.printscript.lexer.rules.TokenRule
import org.printscript.lexer.rules.errorOf

class TokenMatcher(private val rules: List<TokenRule> = PrintScript10.RULES) {
    fun match(
        line: String,
        from: Int,
        lineNumber: Int,
    ): Result<TokenMatch, LexicalError> {
        for (rule in rules) {
            val result = rule.match(line, from, lineNumber)
            if (result != null) return result
        }
        return errorOf("Caracter inesperado '${line[from]}'", lineNumber, from, 1)
    }
}
