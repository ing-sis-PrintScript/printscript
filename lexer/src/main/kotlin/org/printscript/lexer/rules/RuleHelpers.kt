package org.printscript.lexer.rules

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.token.Token
import org.printscript.token.TokenType

/** Armado de tokens y errores, compartido por todas las reglas. */

internal fun tokenOf(type: TokenType, text: String, line: Int, index: Int): Result<Token, LexicalError> =
    Result.Success(Token(type, text, rangeOf(line, index, text.length)))

internal fun errorOf(message: String, line: Int, index: Int, length: Int): Result<Token, LexicalError> =
    Result.Failure(LexicalError(message, rangeOf(line, index, length)))

/** Traduce índice 0-based + largo a un Range de columnas 1-based. */
internal fun rangeOf(line: Int, index: Int, length: Int): Range {
    val startColumn = index + 1
    return Range(Position(line, startColumn), Position(line, startColumn + length - 1))
}
