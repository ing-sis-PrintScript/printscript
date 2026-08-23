package org.printscript.parser

import org.printscript.common.Result
import org.printscript.parser.token.TokenStream
import org.printscript.token.TokenType


fun interface RecoveryStrategy {

    fun recover(stream: TokenStream): TokenStream
}


object SkipToSemicolon : RecoveryStrategy {

    override fun recover(stream: TokenStream): TokenStream = skipPastSemicolon(stream)


    private tailrec fun skipPastSemicolon(stream: TokenStream): TokenStream {
        if (stream.atEnd()) return stream

        val token = stream.peek()
        val rest = stream.advance()

        val isSemicolon = token is Result.Success && token.value.type == TokenType.SEMICOLON
        return if (isSemicolon) rest else skipPastSemicolon(rest)
    }
}
