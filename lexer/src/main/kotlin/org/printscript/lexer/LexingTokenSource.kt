package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.lexer.source.ScanResult
import org.printscript.lexer.source.SourceCursor
import org.printscript.token.ListTokenSource
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import org.printscript.token.TokenType

internal data class LexingTokenSource(
    private val matcher: TokenMatcher,
    private val cursor: SourceCursor,
) : TokenSource {
    override fun nextToken(): TokenReadResult =
        when (val scan = cursor.moveToNextToken()) {
            is ScanResult.Found -> readTokenAt(scan.cursor)
            is ScanResult.Exhausted -> endOfFileAt(scan.endPosition)
        }

    private fun readTokenAt(at: SourceCursor): TokenReadResult =
        when (val match = matcher.match(at.line, at.index, at.lineNumber)) {
            is Result.Success ->
                TokenReadResult.Success(
                    match.value.token,
                    LexingTokenSource(matcher, at.advanceTo(match.value.nextIndex)),
                )

            is Result.Failure -> TokenReadResult.Failure(match.error, noMoreTokens())
        }

    private fun endOfFileAt(end: Position): TokenReadResult =
        TokenReadResult.Success(Token(TokenType.EOF, "", Range(end, end)), noMoreTokens())

    private fun noMoreTokens(): TokenSource = ListTokenSource(emptyList())
}
