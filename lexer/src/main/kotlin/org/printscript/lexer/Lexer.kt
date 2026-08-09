package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.getOrNull
import org.printscript.token.Token
import org.printscript.token.TokenType

/**
 * Convierte el código fuente en tokens, de a uno por vez.
 *
 * Recibe las líneas como Sequence y devuelve una Sequence: nunca tiene el archivo
 * entero ni la lista completa de tokens en memoria. Cada token se calcula recién
 * cuando alguien lo pide.
 *
 * Si algo no se entiende devuelve un Result.Failure como un elemento más de la
 * secuencia, y ahí corta. Nunca tira excepciones.
 */
class Lexer {

    /** Recorre el texto: saltea espacios, pide un token y avanza. Nada más. */
    fun tokenize(lines: Sequence<String>): Sequence<Result<Token, LexicalError>> = sequence {
        var lineNumber = 0
        var endPosition = Position(1, 1)

        for (line in lines) {
            lineNumber++
            var i = 0 // posición dentro de la línea, desde 0

            while (i < line.length) {
                if (line[i].isWhitespace()) {
                    i++
                    continue
                }

                val result = readToken(line, i, lineNumber)
                yield(result)

                // si fue error no tiene sentido seguir; si no, avanzo lo que ocupó el token
                val token = result.getOrNull() ?: return@sequence
                i += token.lexeme.length
            }

            endPosition = Position(lineNumber, line.length + 1)
        }

        // Token final. Le avisa al parser "hasta acá llegó el archivo".
        yield(Result.Success(Token(TokenType.EOF, "", Range(endPosition, endPosition))))
    }

    /** Atajo para cuando el fuente ya está en memoria (tests, strings cortos). */
    fun tokenize(source: String): Sequence<Result<Token, LexicalError>> =
        tokenize(source.lineSequence())

    /**
     * Reconoce el token que empieza en [from]. No avanza nada: de eso se encarga
     * tokenize, usando el largo del lexema.
     */
    private fun readToken(line: String, from: Int, lineNumber: Int): Result<Token, LexicalError> {
        val c = line[from]

        // números: 12, 3.5
        if (c.isDigit()) {
            return tokenAt(TokenType.NUMBER_LITERAL, readNumber(line, from), lineNumber, from)
        }

        // palabras: keyword si está en el mapa, si no identificador
        if (c.isLetter() || c == '_') {
            val text = readWord(line, from)
            return tokenAt(KEYWORDS[text] ?: TokenType.IDENTIFIER, text, lineNumber, from)
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
        val symbolType = SYMBOLS[c]
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

    companion object {
        private val KEYWORDS = mapOf(
            "let" to TokenType.LET,
            "println" to TokenType.PRINTLN,
            "number" to TokenType.TYPE_NUMBER,
            "string" to TokenType.TYPE_STRING,
        )

        private val SYMBOLS = mapOf(
            ':' to TokenType.COLON,
            ';' to TokenType.SEMICOLON,
            '=' to TokenType.ASSIGN,
            '(' to TokenType.LPAREN,
            ')' to TokenType.RPAREN,
            '+' to TokenType.PLUS,
            '-' to TokenType.MINUS,
            '*' to TokenType.STAR,
            '/' to TokenType.SLASH,
        )
    }
}
