package org.printscript.parser

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.Expression
import org.printscript.ast.UnaryExpression
import org.printscript.ast.UnaryOperator
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.peekIs
import org.printscript.token.TokenType

/**
 * La precedencia de los operadores: qué se agrupa con qué. Los valores sueltos
 * sobre los que operan los reconoce el FactorParser.
 *
 * No se llama PrintScript10 porque no es de una versión: las dos la usan tal
 * cual. La precedencia de PrintScript no cambió entre 1.0 y 1.1, y lo único
 * que sí cambia — qué palabras empiezan una llamada — esta clase ni lo mira,
 * solo se lo pasa al FactorParser.
 */
class PrecedenceExpressionParser(
    callTokens: Set<TokenType> = emptySet(),
) : ExpressionParser {
    private val factors = FactorParser(callTokens)

    override fun parse(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> = parseExpression(stream)

    private fun parseExpression(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> =
        parseTerm(stream).flatMap { term -> parseAdditions(term) }

    private tailrec fun parseAdditions(left: Parsed<Expression>): Result<Parsed<Expression>, PrintScriptError> {
        val operator =
            when {
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
        parseUnary(stream).flatMap { unary -> parseMultiplications(unary) }

    private tailrec fun parseMultiplications(left: Parsed<Expression>): Result<Parsed<Expression>, PrintScriptError> {
        val operator =
            when {
                left.rest.peekIs(TokenType.STAR) -> BinaryOperator.TIMES
                left.rest.peekIs(TokenType.SLASH) -> BinaryOperator.DIVIDE
                else -> return Result.Success(left)
            }

        return when (val right = parseUnary(left.rest.advance())) {
            is Result.Failure -> right
            is Result.Success -> parseMultiplications(combine(left.value, operator, right.value))
        }
    }

    private fun parseUnary(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> {
        val tokenResult = stream.peek()
        if (tokenResult is Result.Failure) return tokenResult
        val token = (tokenResult as Result.Success).value

        if (token.type != TokenType.MINUS) return factors.parse(stream, this)

        val operandResult = parseUnary(stream.advance())
        if (operandResult is Result.Failure) return operandResult
        val (operand, rest) = (operandResult as Result.Success).value

        val range = Range(token.range.start, operand.range.end)
        return Result.Success(Parsed(UnaryExpression(UnaryOperator.MINUS, operand, range), rest))
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
