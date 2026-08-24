package org.printscript.lexer

import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.getOrNull
import org.printscript.common.map
import org.printscript.lexer.source.SourceCursor
import org.printscript.token.Token
import org.printscript.token.TokenType

class Lexer(private val matcher: TokenMatcher = TokenMatcher()) {
    fun tokenize(lines: Sequence<String>): Sequence<Result<Token, LexicalError>> =
        sequence {
            val cursor = SourceCursor(lines)

            while (cursor.moveToNextToken()) {
                val result = matcher.match(cursor.line, cursor.index, cursor.lineNumber)
                yield(result.map { it.token })

                val match = result.getOrNull() ?: return@sequence
                cursor.advanceTo(match.nextIndex)
            }

            val end = cursor.endPosition()
            yield(Result.Success(Token(TokenType.EOF, "", Range(end, end))))
        }

    fun tokenize(source: String): Sequence<Result<Token, LexicalError>> = tokenize(source.lineSequence())
}
