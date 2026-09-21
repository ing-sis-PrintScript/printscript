package org.printscript.formatter.engine

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.Formatter
import org.printscript.formatter.config.Indent
import org.printscript.formatter.rules.FormattingState
import org.printscript.formatter.rules.SpacingMatcher
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import org.printscript.token.TokenType

class TokenFormatter(
    private val matcher: SpacingMatcher = SpacingMatcher(),
    private val indent: Indent? = null,
) : Formatter {
    override fun format(tokens: TokenSource): Sequence<Result<FormattedCode, PrintScriptError>> =
        FormattedTokens(tokens, matcher, indent)
}

private fun sourceTextOf(token: Token): String =
    when (token.type) {
        TokenType.STRING_LITERAL -> "\"${token.value}\""
        else -> token.value
    }

private fun render(
    matcher: SpacingMatcher,
    indent: Indent?,
    prev: Token?,
    token: Token,
    state: FormattingState,
): FormattedCode {
    val spacing = matcher.spacingFor(prev, token, state) ?: state.pendingWhitespace
    return FormattedCode(indented(spacing, indent, token, state) + sourceTextOf(token))
}

private fun indented(
    spacing: String,
    indent: Indent?,
    token: Token,
    state: FormattingState,
): String {
    if (!spacing.contains('\n')) return spacing

    val lineBreaks = spacing.substringBeforeLast('\n') + "\n"
    if (indent == null) return lineBreaks + sourceIndentOf(state.pendingWhitespace)

    val depth = if (token.type == TokenType.RBRACE) state.blockDepth - 1 else state.blockDepth

    return lineBreaks + indent.render(depth.coerceAtLeast(0))
}

private fun sourceIndentOf(whitespace: String): String = whitespace.substringAfterLast('\n', missingDelimiterValue = "")

private class FormattedTokens(
    source: TokenSource,
    private val matcher: SpacingMatcher,
    private val indent: Indent?,
) : Sequence<Result<FormattedCode, PrintScriptError>> {
    private var start: TokenSource? = source

    override fun iterator(): Iterator<Result<FormattedCode, PrintScriptError>> {
        val first = requireNotNull(start) { "esta secuencia se recorre una sola vez" }
        start = null

        return object : AbstractIterator<Result<FormattedCode, PrintScriptError>>() {
            private var pending: TokenSource? = first

            private var previous: Token? = null

            private var currentHead: TokenType? = null
            private var state = FormattingState()

            override fun computeNext() {
                while (true) {
                    when (val read = pending?.nextToken()) {
                        is TokenReadResult.Success -> {
                            pending = read.remaining
                            if (emit(read.token)) return
                        }

                        is TokenReadResult.Failure -> {
                            pending = null
                            setNext(Result.Failure(read.error))
                            return
                        }

                        TokenReadResult.EndOfInput, null -> {
                            done()
                            return
                        }
                    }
                }
            }

            private fun emit(token: Token): Boolean {
                if (token.type == TokenType.WHITESPACE) {
                    state = state.copy(pendingWhitespace = token.value)
                    return false
                }

                val formatted = render(matcher, indent, previous, token, state)
                previous = token
                advanceStatement(token)
                state = state.copy(pendingWhitespace = "")
                setNext(Result.Success(formatted))
                return true
            }

            private fun advanceStatement(token: Token) {
                when (token.type) {
                    TokenType.SEMICOLON -> {
                        state = state.copy(lastStatementHead = currentHead)
                        currentHead = null
                    }

                    TokenType.LBRACE -> {
                        state = state.copy(blockDepth = state.blockDepth + 1, lastStatementHead = null)
                        currentHead = null
                    }

                    TokenType.RBRACE -> {
                        state = state.copy(blockDepth = state.blockDepth - 1, lastStatementHead = null)
                        currentHead = null
                    }

                    else -> if (currentHead == null && token.type != TokenType.EOF) currentHead = token.type
                }
            }
        }
    }
}
