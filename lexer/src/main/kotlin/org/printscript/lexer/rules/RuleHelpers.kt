package org.printscript.lexer.rules

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.lexer.TokenMatch
import org.printscript.token.Token
import org.printscript.token.TokenType

internal fun matchOf(
    type: TokenType,
    text: String,
    line: Int,
    index: Int,
): Result<TokenMatch, LexicalError> = matchOf(type, text, text.length, line, index)

internal fun matchOf(
    type: TokenType,
    value: String,
    rawLength: Int,
    line: Int,
    index: Int,
): Result<TokenMatch, LexicalError> =
    Result.Success(
        TokenMatch(
            token = Token(type, value, rangeOf(line, index, rawLength)),
            nextIndex = index + rawLength,
        ),
    )

internal fun errorOf(
    message: String,
    line: Int,
    index: Int,
    length: Int,
): Result<TokenMatch, LexicalError> = Result.Failure(LexicalError(message, rangeOf(line, index, length)))

internal fun rangeOf(
    line: Int,
    index: Int,
    length: Int,
): Range {
    val startColumn = index + 1
    return Range(Position(line, startColumn), Position(line, startColumn + length - 1))
}
