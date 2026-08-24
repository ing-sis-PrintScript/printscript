package org.printscript.parser

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.statements.StatementParser
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.describe
import org.printscript.token.Token

class Parser(
    private val statementParsers: List<StatementParser>,
) {
    fun parse(tokens: Sequence<Result<Token, PrintScriptError>>): Sequence<Result<ASTNode, PrintScriptError>> =
        sequence {
            val stream = TokenStream(tokens)

            while (!stream.atEnd()) {
                val result = parseStatement(stream)
                yield(result)

                if (result is Result.Failure) stream.synchronize()
            }
        }

    private fun parseStatement(stream: TokenStream): Result<ASTNode, PrintScriptError> {
        val peeked = stream.peek()
        if (peeked is Result.Failure) {
            stream.next() // consumirlo, para que synchronize no lo vuelva a leer
            return peeked
        }

        val token = (peeked as Result.Success).value
        val parser =
            statementParsers.firstOrNull { it.canHandle(token.type) }
                ?: return Result.Failure(
                    SyntaxError("No se esperaba ${token.type.describe()} acá", token.range),
                )

        return parser.parse(stream)
    }
}
