package org.printscript.parser

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.statements.StatementParser
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.describe
import org.printscript.token.TokenSource

class Parser(
    private val statementParsers: List<StatementParser>,
    private val recovery: RecoveryStrategy,
) {
    fun parse(source: TokenSource): Sequence<Result<Statement, PrintScriptError>> =
        generateSequence({ step(TokenStream(source)) }) { previous -> step(previous.rest) }
            .map { it.value }

    private fun step(stream: TokenStream): Parsed<Result<Statement, PrintScriptError>>? {
        if (stream.atEnd()) return null

        return when (val result = parseStatement(stream)) {
            is Result.Success -> Parsed(Result.Success(result.value.value), result.value.rest)
            is Result.Failure -> Parsed(result, recovery.recover(stream))
        }
    }

    private fun parseStatement(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> {
        val peeked = stream.peek()
        if (peeked is Result.Failure) return peeked

        val token = (peeked as Result.Success).value
        val parser =
            statementParsers.firstOrNull { it.canHandle(token.type) }
                ?: return Result.Failure(
                    SyntaxError("No se esperaba ${token.type.describe()} acá", token.range),
                )

        return parser.parse(stream)
    }
}
