package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.getOrNull
import org.printscript.common.map
import org.printscript.token.Token
import org.printscript.token.TokenType

/**
 * Recorre el codigo fuente y emite tokens, de a uno por vez.
 *
 * Recibe las lineas como Sequence y devuelve una Sequence: nunca tiene el
 * archivo entero ni la lista completa de tokens en memoria. Cada token se
 * calcula recien cuando alguien lo pide.
 *
 * No sabe que es una keyword, ni como se escribe un numero, ni cuanto ocupa un
 * token: todo eso lo deciden las reglas, que ademas le dicen desde que indice
 * seguir. Aca solo se recorre, se saltean espacios y se avanza a donde indican.
 */
class Lexer(private val matcher: TokenMatcher = TokenMatcher()) {
    fun tokenize(lines: Sequence<String>): Sequence<Result<Token, LexicalError>> =
        sequence {
            var lineNumber = 0
            var endPosition = Position(1, 1)

            for (line in lines) {
                lineNumber++
                var i = 0 // posicion dentro de la linea, desde 0

                while (i < line.length) {
                    if (line[i].isWhitespace()) {
                        i++
                        continue
                    }

                    val result = matcher.match(line, i, lineNumber)
                    yield(result.map { it.token })

                    // si fue error no tiene sentido seguir; si no, la regla dice donde sigo
                    val match = result.getOrNull() ?: return@sequence
                    i = match.nextIndex
                }

                endPosition = Position(lineNumber, line.length + 1)
            }

            // Token final. Le avisa al parser "hasta aca llego el archivo".
            yield(Result.Success(Token(TokenType.EOF, "", Range(endPosition, endPosition))))
        }

    /** Atajo para cuando el fuente ya esta en memoria (tests, strings cortos). */
    fun tokenize(source: String): Sequence<Result<Token, LexicalError>> = tokenize(source.lineSequence())
}
