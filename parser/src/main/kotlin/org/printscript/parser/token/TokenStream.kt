package org.printscript.parser.token

import org.printscript.common.Position
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.token.Token
import org.printscript.token.TokenType

interface TokenStream {

    fun peek(): Result<Token, PrintScriptError>

    fun advance(): TokenStream

    fun atEnd(): Boolean

    companion object {
        fun of(tokens: Sequence<Result<Token, PrintScriptError>>): TokenStream =
            from(tokens.iterator(), START)
    }
}

private val START = Range(Position(1, 1), Position(1, 1))

private fun from(
    tokens: Iterator<Result<Token, PrintScriptError>>,
    previous: Range,
): TokenStream {
    if (!tokens.hasNext()) return Exhausted(previous)

    val head = tokens.next()
    return Node(head) { from(tokens, rangeOf(head, previous)) }
}

private fun rangeOf(head: Result<Token, PrintScriptError>, previous: Range): Range =
    if (head is Result.Success) head.value.range else previous

private class Node(
    private val head: Result<Token, PrintScriptError>,
    rest: () -> TokenStream,
) : TokenStream {

    private val tail: TokenStream by lazy(rest)

    override fun peek(): Result<Token, PrintScriptError> = head

    override fun advance(): TokenStream = tail

    override fun atEnd(): Boolean = head is Result.Success && head.value.type == TokenType.EOF
}

private class Exhausted(private val previous: Range) : TokenStream {

    override fun peek(): Result<Token, PrintScriptError> = Result.Failure(UnexpectedEndOfInput(previous))

    override fun advance(): TokenStream = this

    override fun atEnd(): Boolean = true
}
