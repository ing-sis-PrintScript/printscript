package org.printscript.parser.statements

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.SyntaxError
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.describe

fun interface StatementParsers {
    fun all(): List<StatementParser>

    fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> {
        val peeked = stream.peek()
        if (peeked is Result.Failure) return peeked

        val token = (peeked as Result.Success).value
        val parser =
            all().firstOrNull { it.canHandle(token.type) }
                ?: return Result.Failure(
                    SyntaxError("No se esperaba ${token.type.describe()} acá", token.range),
                )

        return parser.parse(stream)
    }
}
