package org.printscript.parser.token

import org.printscript.common.Position
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import org.printscript.token.TokenType

private val START = Range(Position(1, 1), Position(1, 1))

data class TokenStream(
    private val current: TokenReadResult,
    private val lastRange: Range = START,
) {
    constructor(source: TokenSource) : this(source.nextToken())

    fun peek(): Result<Token, PrintScriptError> =
        when (current) {
            is TokenReadResult.Success -> Result.Success(current.token)
            is TokenReadResult.Failure -> Result.Failure(current.error)
            TokenReadResult.EndOfInput -> Result.Failure(UnexpectedEndOfInput(lastRange))
        }

    fun advance(): TokenStream =
        when (current) {
            is TokenReadResult.Success -> TokenStream(current.remaining.nextToken(), current.token.range)
            is TokenReadResult.Failure -> TokenStream(current.remaining.nextToken(), lastRange)
            TokenReadResult.EndOfInput -> this
        }

    fun atEnd(): Boolean =
        current is TokenReadResult.EndOfInput ||
            (current is TokenReadResult.Success && current.token.type == TokenType.EOF)
}
