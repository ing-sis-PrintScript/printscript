package org.printscript.lexer

import org.printscript.common.Result
import org.printscript.lexer.rules.TokenRule
import org.printscript.lexer.rules.errorOf

/**
 * Reconoce la unidad lexica que empieza en una posicion dada, preguntandole a
 * cada regla por orden hasta que una conteste.
 *
 * No sabe que es un numero ni que es una keyword: eso lo saben las reglas. Y no
 * sabe de lineas ni de streaming: eso lo sabe el Lexer.
 */
class TokenMatcher(private val rules: List<TokenRule> = PrintScript10.RULES) {

    /** No avanza nada: devuelve donde sigue, y el que llama decide que hacer. */
    fun match(line: String, from: Int, lineNumber: Int): Result<TokenMatch, LexicalError> {
        for (rule in rules) {
            val result = rule.match(line, from, lineNumber)
            if (result != null) return result
        }
        return errorOf("Caracter inesperado '${line[from]}'", lineNumber, from, 1)
    }
}
