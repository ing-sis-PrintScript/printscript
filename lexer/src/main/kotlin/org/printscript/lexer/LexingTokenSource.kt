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
            is ScanResult.Found -> foundAt(scan.cursor)
            is ScanResult.Exhausted -> endOfFileAt(scan.endPosition)
        }

    // Si quedo espacio atras, sale primero y solo. El token que venia detras se
    // entrega en la llamada siguiente: el cursor ya avanzo hasta el, lo unico que
    // cambia es que ahora no tiene espacio pendiente.
    private fun foundAt(at: SourceCursor): TokenReadResult =
        if (at.skipped.isEmpty()) readTokenAt(at) else whitespaceAt(at)

    private fun whitespaceAt(at: SourceCursor): TokenReadResult =
        TokenReadResult.Success(
            Token(TokenType.WHITESPACE, at.skipped, at.skippedRange),
            LexingTokenSource(matcher, at.clearSkipped()),
        )

    private fun readTokenAt(at: SourceCursor): TokenReadResult =
        when (val match = matcher.match(at.line, at.index, at.lineNumber)) {
            is Result.Success ->
                TokenReadResult.Success(
                    match.value.token,
                    LexingTokenSource(matcher, at.advanceTo(match.value.nextIndex)),
                )

            is Result.Failure -> TokenReadResult.Failure(match.error, noMoreTokens())
        }

    // El espacio del final del archivo no sale como token: scan() termina en
    // Exhausted y lo descarta. Es a proposito --el salto final cierra la ultima
    // linea, no abre una vacia-- y es lo que evita que el formatter escriba un
    // salto de mas al final de cada golden.
    private fun endOfFileAt(end: Position): TokenReadResult =
        TokenReadResult.Success(Token(TokenType.EOF, "", Range(end, end)), noMoreTokens())

    private fun noMoreTokens(): TokenSource = ListTokenSource(emptyList())
}
