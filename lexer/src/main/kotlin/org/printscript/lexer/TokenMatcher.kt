package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.token.Token
import org.printscript.token.TokenType

/**
 * Reconoce el token que empieza en una posición dada.
 *
 * No sabe nada de líneas, de secuencias ni de streaming: le das una línea y un
 * índice, y te dice qué token hay ahí. Recorrer el texto es problema del Lexer.
 *
 * El vocabulario entra por constructor, así que la misma clase sirve para
 * cualquier versión del lenguaje.
 */
class TokenMatcher(
    private val keywords: Map<String, TokenType> = PrintScript10.KEYWORDS,
    private val symbols: Map<Char, TokenType> = PrintScript10.SYMBOLS,
) {

    /** No avanza nada: el que llama avanza usando el largo del lexema. */
    fun match(line: String, from: Int, lineNumber: Int): Result<Token, LexicalError> {
        val c = line[from]

        // números: 12, 3.5
        if (c.isDigit()) {
            return tokenAt(TokenType.NUMBER_LITERAL, readNumber(line, from), lineNumber, from)
        }

        // palabras: keyword si está en el mapa, si no identificador
        if (c.isLetter() || c == '_') {
            val text = readWord(line, from)
            return tokenAt(keywords[text] ?: TokenType.IDENTIFIER, text, lineNumber, from)
        }

        // strings: "hola" o 'hola'. Tiene que cerrar en la misma línea.
        if (c == '"' || c == '\'') {
            val closing = line.indexOf(c, from + 1)
            if (closing == -1) {
                return errorAt("String sin cerrar", lineNumber, from, line.length - from)
            }
            return tokenAt(TokenType.STRING_LITERAL, line.substring(from, closing + 1), lineNumber, from)
        }

        // símbolos de un solo caracter: : ; = ( ) + - * /
        val symbolType = symbols[c]
        if (symbolType != null) {
            return tokenAt(symbolType, c.toString(), lineNumber, from)
        }

        return errorAt("Caracter inesperado '$c'", lineNumber, from, 1)
    }

    /** Lee dígitos, y si hay un punto seguido de más dígitos también los lee. Ej: "3.5" */
    private fun readNumber(line: String, from: Int): String {
        var i = from
        while (i < line.length && line[i].isDigit()) i++
        if (i < line.length && line[i] == '.') {
            i++
            while (i < line.length && line[i].isDigit()) i++
        }
        return line.substring(from, i)
    }

    /** Lee letras, dígitos y guiones bajos. Ej: "lastName", "x1", "_tmp" */
    private fun readWord(line: String, from: Int): String {
        var i = from
        while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) i++
        return line.substring(from, i)
    }

    private fun tokenAt(type: TokenType, text: String, line: Int, index: Int): Result<Token, LexicalError> =
        Result.Success(Token(type, text, rangeOf(line, index, text.length)))

    private fun errorAt(message: String, line: Int, index: Int, length: Int): Result<Token, LexicalError> =
        Result.Failure(LexicalError(message, rangeOf(line, index, length)))

    /** Traduce índice 0-based + largo a un Range de columnas 1-based. */
    private fun rangeOf(line: Int, index: Int, length: Int): Range {
        val startColumn = index + 1
        return Range(Position(line, startColumn), Position(line, startColumn + length - 1))
    }
}
