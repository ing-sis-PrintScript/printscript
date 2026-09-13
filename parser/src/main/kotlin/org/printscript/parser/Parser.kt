package org.printscript.parser

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.statements.StatementParsers
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.token.TokenSource

class Parser(
    private val statements: StatementParsers,
    private val recovery: RecoveryStrategy,
) {
    fun parse(source: TokenSource): Sequence<Result<Statement, PrintScriptError>> = ParsedStatements(source, ::step)

    private fun step(stream: TokenStream): Parsed<Result<Statement, PrintScriptError>>? {
        if (stream.atEnd()) return null

        return when (val result = statements.parse(stream)) {
            is Result.Success -> Parsed(Result.Success(result.value.value), result.value.rest)
            is Result.Failure -> Parsed(result, recovery.recover(stream))
        }
    }
}

private class ParsedStatements(
    source: TokenSource,
    private val step: (TokenStream) -> Parsed<Result<Statement, PrintScriptError>>?,
) : Sequence<Result<Statement, PrintScriptError>> {
    private var start: TokenSource? = source

    override fun iterator(): Iterator<Result<Statement, PrintScriptError>> {
        val first = requireNotNull(start) { "esta secuencia se recorre una sola vez" }
        start = null

        return object : AbstractIterator<Result<Statement, PrintScriptError>>() {
            private var pending: TokenStream? = TokenStream(first)

            override fun computeNext() {
                val from = pending
                val parsed = if (from == null) null else step(from)

                if (parsed == null) {
                    done()
                } else {
                    pending = parsed.rest
                    setNext(parsed.value)
                }
            }
        }
    }
}
