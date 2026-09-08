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
                    // La trivia la pega el lexer, no las reglas: ellas ven una
                    // linea y un indice, no saben que quedo atras.
                    match.value.token.copy(leadingTrivia = at.trivia),
                    LexingTokenSource(matcher, at.advanceTo(match.value.nextIndex)),
                )

            is Result.Failure -> TokenReadResult.Failure(match.error, noMoreTokens())
        }

    // El EOF va sin trivia a proposito: el salto final del archivo cierra la
    // ultima linea, no abre una vacia. Si termina o no con salto lo decide una
    // regla del formatter, no el lexer.
    private fun endOfFileAt(end: Position): TokenReadResult =
        TokenReadResult.Success(Token(TokenType.EOF, "", Range(end, end)), noMoreTokens())

    private fun noMoreTokens(): TokenSource = ListTokenSource(emptyList())
}
