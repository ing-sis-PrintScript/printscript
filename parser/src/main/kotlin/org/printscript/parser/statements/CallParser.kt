package org.printscript.parser.statements

import org.printscript.ast.CallExpression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.parser.ExpressionParser
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.expect
import org.printscript.parser.token.skip
import org.printscript.token.Token
import org.printscript.token.TokenType

class CallParser(
    private val expressions: ExpressionParser,
) : StatementParser {
    override fun canHandle(type: TokenType): Boolean = type == TokenType.PRINTLN

    override fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> {
        val calleeResult = stream.expect(TokenType.PRINTLN)
        if (calleeResult is Result.Failure) return calleeResult
        val (callee, afterCallee) = (calleeResult as Result.Success).value

        val openResult = afterCallee.skip(TokenType.LPAREN, "después de println")
        if (openResult is Result.Failure) return openResult
        val afterOpen = (openResult as Result.Success).value

        return parseCall(callee, afterOpen)
    }

    private fun parseCall(
        callee: Token,
        stream: TokenStream,
    ): Result<Parsed<Statement>, PrintScriptError> {
        val argumentResult = expressions.parse(stream)
        if (argumentResult is Result.Failure) return argumentResult
        val (argument, afterArgument) = (argumentResult as Result.Success).value

        val rparenResult = afterArgument.expect(TokenType.RPAREN, "para cerrar la llamada")
        if (rparenResult is Result.Failure) return rparenResult
        val (rparen, afterClose) = (rparenResult as Result.Success).value

        val semicolonResult = afterClose.expect(TokenType.SEMICOLON, "al final de la sentencia")
        if (semicolonResult is Result.Failure) return semicolonResult
        val (semicolon, afterSemicolon) = (semicolonResult as Result.Success).value

        val call =
            CallExpression(
                callee = Identifier(callee.value, callee.range),
                arguments = listOf(argument),
                range = Range(callee.range.start, rparen.range.end),
            )
        // El statement incluye el ";", la llamada no.
        val statement =
            ExpressionStatement(
                expression = call,
                range = Range(callee.range.start, semicolon.range.end),
            )
        return Result.Success(Parsed(statement, afterSemicolon))
    }
}
