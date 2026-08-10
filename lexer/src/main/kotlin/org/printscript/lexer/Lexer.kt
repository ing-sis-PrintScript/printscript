package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.getOrNull
import org.printscript.token.Token
import org.printscript.token.TokenType

/**
 * Recorre el código fuente y emite tokens, de a uno por vez.
 *
 * Recibe las líneas como Sequence y devuelve una Sequence: nunca tiene el archivo
 * entero ni la lista completa de tokens en memoria. Cada token se calcula recién
 * cuando alguien lo pide.
 *
 * No sabe qué es una keyword ni cómo se escribe un número: eso lo decide el
 * TokenMatcher. Acá solo se recorre, se saltean espacios y se avanza.
 */
class Lexer(private val matcher: TokenMatcher = TokenMatcher()) {

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

                val result = matcher.match(line, i, lineNumber)
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
}
