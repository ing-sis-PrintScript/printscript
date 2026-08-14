package org.printscript.lexer

import org.printscript.common.Result
import org.printscript.lexer.rules.TokenRule
import org.printscript.lexer.rules.errorOf
import org.printscript.token.Token

/**
 * Reconoce el token que empieza en una posición dada, preguntándole a cada regla
 * por orden hasta que una conteste.
 *
 * No sabe qué es un número ni qué es una keyword: eso lo saben las reglas. Y no
 * sabe de líneas ni de streaming: eso lo sabe el Lexer.
 */
class TokenMatcher(private val rules: List<TokenRule> = PrintScript10.RULES) {

    /** No avanza nada: el que llama avanza usando el largo del lexema. */
    fun match(line: String, from: Int, lineNumber: Int): Result<Token, LexicalError> {
        for (rule in rules) {
            val result = rule.match(line, from, lineNumber)
            if (result != null) return result
        }
        return errorOf("Caracter inesperado '${line[from]}'", lineNumber, from, 1)
    }
}
