package org.printscript.parser.expressions

import org.printscript.ast.BooleanLiteral
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.parser.SyntaxError
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.expect
import org.printscript.parser.token.skip
import org.printscript.token.Token
import org.printscript.token.TokenType

internal class FactorParser(
    private val callTokens: Set<TokenType>,
) {
    fun parse(
        stream: TokenStream,
        expressions: ExpressionParser,
    ): Result<Parsed<Expression>, PrintScriptError> =
        stream.peek().flatMap { token ->
            when (token.type) {
                TokenType.NUMBER_LITERAL -> numberLiteral(token, stream.advance())

                TokenType.STRING_LITERAL ->
                    Result.Success(Parsed(StringLiteral(token.value, token.range), stream.advance()))

                TokenType.BOOLEAN_LITERAL ->
                    Result.Success(Parsed(BooleanLiteral(token.value == "true", token.range), stream.advance()))

                TokenType.IDENTIFIER ->
                    Result.Success(Parsed(Identifier(token.value, token.range), stream.advance()))

                TokenType.LPAREN -> parenthesized(stream, expressions)

                in callTokens -> call(token, stream, expressions)

                else ->
                    Result.Failure(
                        SyntaxError("Se esperaba un valor, un identificador o '('", token.range),
                    )
            }
        }

    private fun call(
        callee: Token,
        stream: TokenStream,
        expressions: ExpressionParser,
    ): Result<Parsed<Expression>, PrintScriptError> {
        val openResult = stream.advance().skip(TokenType.LPAREN, "después de '${callee.value}'")
        if (openResult is Result.Failure) return openResult

        val argumentResult = expressions.parse((openResult as Result.Success).value)
        if (argumentResult is Result.Failure) return argumentResult
        val (argument, afterArgument) = (argumentResult as Result.Success).value

        val closeResult = afterArgument.expect(TokenType.RPAREN, "para cerrar la llamada")
        if (closeResult is Result.Failure) return closeResult
        val (rparen, afterClose) = (closeResult as Result.Success).value

        val expression =
            CallExpression(
                callee = Identifier(callee.value, callee.range),
                arguments = listOf(argument),
                range = Range(callee.range.start, rparen.range.end),
            )
        return Result.Success(Parsed(expression, afterClose))
    }

    private fun numberLiteral(
        token: Token,
        rest: TokenStream,
    ): Result<Parsed<Expression>, PrintScriptError> {
        val number =
            token.value.toDoubleOrNull()
                ?: return Result.Failure(
                    SyntaxError("'${token.value}' no es un número válido", token.range),
                )

        return Result.Success(Parsed(NumberLiteral(number, token.range), rest))
    }

    private fun parenthesized(
        stream: TokenStream,
        expressions: ExpressionParser,
    ): Result<Parsed<Expression>, PrintScriptError> {
        val openResult = stream.skip(TokenType.LPAREN)
        if (openResult is Result.Failure) return openResult
        val afterOpen = (openResult as Result.Success).value

        val innerResult = expressions.parse(afterOpen)
        if (innerResult is Result.Failure) return innerResult
        val (inner, afterInner) = (innerResult as Result.Success).value

        val closeResult = afterInner.skip(TokenType.RPAREN)
        if (closeResult is Result.Failure) return closeResult
        return Result.Success(Parsed(inner, (closeResult as Result.Success).value))
    }
}
