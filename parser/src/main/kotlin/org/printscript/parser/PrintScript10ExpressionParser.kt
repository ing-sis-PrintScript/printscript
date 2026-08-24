package org.printscript.parser

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.parser.token.TokenStream
import org.printscript.token.Token
import org.printscript.token.TokenType

class PrintScript10ExpressionParser : ExpressionParser {
    override fun parse(stream: TokenStream): Result<Expression, PrintScriptError> = parseExpression(stream)

    private fun parseExpression(stream: TokenStream): Result<Expression, PrintScriptError> {
        var left = parseTerm(stream)

        while (left is Result.Success && (stream.peekIs(TokenType.PLUS) || stream.peekIs(TokenType.MINUS))) {
            val operator = if (stream.peekIs(TokenType.PLUS)) BinaryOperator.PLUS else BinaryOperator.MINUS
            stream.next()

            left = combine(left.value, operator, parseTerm(stream))
        }

        return left
    }

    private fun parseTerm(stream: TokenStream): Result<Expression, PrintScriptError> {
        var left = parseFactor(stream)

        while (left is Result.Success && (stream.peekIs(TokenType.STAR) || stream.peekIs(TokenType.SLASH))) {
            val operator = if (stream.peekIs(TokenType.STAR)) BinaryOperator.TIMES else BinaryOperator.DIVIDE
            stream.next()

            left = combine(left.value, operator, parseFactor(stream))
        }

        return left
    }

    private fun parseFactor(stream: TokenStream): Result<Expression, PrintScriptError> {
        val peeked = stream.peek()
        if (peeked is Result.Failure) return peeked
        val token = (peeked as Result.Success).value

        return when (token.type) {
            TokenType.NUMBER_LITERAL ->
                stream.next().flatMap { numberLiteral(it) }

            // token.value ya viene sin comillas: son delimitadores, no contenido.
            TokenType.STRING_LITERAL ->
                stream.next().flatMap { Result.Success(StringLiteral(it.value, it.range)) }

            TokenType.IDENTIFIER ->
                stream.next().flatMap { Result.Success(Identifier(it.value, it.range)) }

            TokenType.LPAREN -> parenthesized(stream)

            else ->
                Result.Failure(
                    SyntaxError("Se esperaba un valor, un identificador o '('", token.range),
                )
        }
    }

    private fun numberLiteral(token: Token): Result<Expression, PrintScriptError> {
        val number =
            token.value.toDoubleOrNull()
                ?: return Result.Failure(
                    SyntaxError("'${token.value}' no es un número válido", token.range),
                )

        return Result.Success(NumberLiteral(number, token.range))
    }

    private fun parenthesized(stream: TokenStream): Result<Expression, PrintScriptError> =
        stream.skip(TokenType.LPAREN, "'('").flatMap {
            parseExpression(stream).flatMap { inner ->
                stream.skip(TokenType.RPAREN, "')' para cerrar la expresión")
                    .flatMap { Result.Success(inner) }
            }
        }

    private fun combine(
        left: Expression,
        operator: BinaryOperator,
        right: Result<Expression, PrintScriptError>,
    ): Result<Expression, PrintScriptError> =
        right.flatMap {
            Result.Success(BinaryExpression(operator, left, it, Range(left.range.start, it.range.end)))
        }
}
