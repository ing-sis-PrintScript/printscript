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
import org.printscript.common.map
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.peekIs
import org.printscript.parser.token.skip
import org.printscript.token.Token
import org.printscript.token.TokenType

class PrintScript10ExpressionParser : ExpressionParser {

    override fun parse(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> = parseExpression(stream)

    private fun parseExpression(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> =
        parseTerm(stream).flatMap { term -> parseAdditions(term) }


    private tailrec fun parseAdditions(left: Parsed<Expression>): Result<Parsed<Expression>, PrintScriptError> {
        val operator = when {
            left.rest.peekIs(TokenType.PLUS) -> BinaryOperator.PLUS
            left.rest.peekIs(TokenType.MINUS) -> BinaryOperator.MINUS
            else -> return Result.Success(left)
        }

        return when (val right = parseTerm(left.rest.advance())) {
            is Result.Failure -> right
            is Result.Success -> parseAdditions(combine(left.value, operator, right.value))
        }
    }

    private fun parseTerm(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> =
        parseFactor(stream).flatMap { factor -> parseMultiplications(factor) }

    private tailrec fun parseMultiplications(left: Parsed<Expression>): Result<Parsed<Expression>, PrintScriptError> {
        val operator = when {
            left.rest.peekIs(TokenType.STAR) -> BinaryOperator.TIMES
            left.rest.peekIs(TokenType.SLASH) -> BinaryOperator.DIVIDE
            else -> return Result.Success(left)
        }

        return when (val right = parseFactor(left.rest.advance())) {
            is Result.Failure -> right
            is Result.Success -> parseMultiplications(combine(left.value, operator, right.value))
        }
    }

    private fun parseFactor(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> =
        stream.peek().flatMap { token ->
            when (token.type) {
                TokenType.NUMBER_LITERAL ->
                    numberLiteral(token).map { Parsed(it, stream.advance()) }

                // token.value ya viene sin comillas: son delimitadores, no contenido.
                TokenType.STRING_LITERAL ->
                    Result.Success(Parsed(StringLiteral(token.value, token.range), stream.advance()))

                TokenType.IDENTIFIER ->
                    Result.Success(Parsed(Identifier(token.value, token.range), stream.advance()))

                TokenType.LPAREN -> parenthesized(stream)

                else -> Result.Failure(
                    SyntaxError("Se esperaba un valor, un identificador o '('", token.range),
                )
            }
        }


    private fun numberLiteral(token: Token): Result<Expression, PrintScriptError> {
        val number = token.value.toDoubleOrNull()
            ?: return Result.Failure(
                SyntaxError("'${token.value}' no es un número válido", token.range),
            )

        return Result.Success(NumberLiteral(number, token.range))
    }


    private fun parenthesized(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> =
        stream.skip(TokenType.LPAREN).flatMap { afterOpen ->
            parseExpression(afterOpen).flatMap { (inner, afterInner) ->
                afterInner.skip(TokenType.RPAREN).map { afterClose -> Parsed(inner, afterClose) }
            }
        }


    private fun combine(
        left: Expression,
        operator: BinaryOperator,
        right: Parsed<Expression>,
    ): Parsed<Expression> =
        Parsed(
            BinaryExpression(operator, left, right.value, Range(left.range.start, right.value.range.end)),
            right.rest,
        )
}
