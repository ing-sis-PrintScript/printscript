package org.printscript.lexer

import org.printscript.lexer.source.SourceCursor
import org.printscript.lexer.source.SourceReader
import org.printscript.lexer.source.StringSourceReader
import org.printscript.token.TokenSource

class Lexer(private val matcher: TokenMatcher = TokenMatcher()) {
    fun tokenize(source: SourceReader): TokenSource = LexingTokenSource(matcher, SourceCursor.from(source))

    fun tokenize(source: String): TokenSource = tokenize(StringSourceReader(source))
}
