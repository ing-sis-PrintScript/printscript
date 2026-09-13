package org.printscript.parser.statements

import org.printscript.ast.Expression
import org.printscript.ast.IfStatement
import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.parser.expressions.ExpressionParser
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.expect
import org.printscript.parser.token.peekIs
import org.printscript.parser.token.skip
import org.printscript.token.Token
import org.printscript.token.TokenType

class IfParser(
    private val expressions: ExpressionParser,
    private val blocks: BlockParser,
) : StatementParser {
    override fun canHandle(type: TokenType): Boolean = type == TokenType.IF

    override fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> {
        val keywordResult = stream.expect(TokenType.IF)
        if (keywordResult is Result.Failure) return keywordResult
        val (keyword, afterKeyword) = (keywordResult as Result.Success).value

        val conditionResult = parseCondition(afterKeyword)
        if (conditionResult is Result.Failure) return conditionResult
        val (condition, afterCondition) = (conditionResult as Result.Success).value

        val thenResult = blocks.parse(afterCondition)
        if (thenResult is Result.Failure) return thenResult
        val (thenBranch, afterThen) = (thenResult as Result.Success).value

        return finishIf(keyword, condition, thenBranch, afterThen)
    }

    private fun parseCondition(stream: TokenStream): Result<Parsed<Expression>, PrintScriptError> {
        val openResult = stream.skip(TokenType.LPAREN, "después del if")
        if (openResult is Result.Failure) return openResult

        val conditionResult = expressions.parse((openResult as Result.Success).value)
        if (conditionResult is Result.Failure) return conditionResult
        val (condition, afterCondition) = (conditionResult as Result.Success).value

        val closeResult = afterCondition.skip(TokenType.RPAREN, "para cerrar la condición")
        if (closeResult is Result.Failure) return closeResult

        return Result.Success(Parsed(condition, (closeResult as Result.Success).value))
    }

    private fun finishIf(
        keyword: Token,
        condition: Expression,
        thenBranch: BlockParser.Block,
        stream: TokenStream,
    ): Result<Parsed<Statement>, PrintScriptError> {
        if (!stream.peekIs(TokenType.ELSE)) {
            val statement = ifStatement(keyword, condition, thenBranch.statements, null, thenBranch.close)
            return Result.Success(Parsed(statement, stream))
        }

        val elseResult = blocks.parse(stream.advance())
        if (elseResult is Result.Failure) return elseResult
        val (elseBranch, afterElse) = (elseResult as Result.Success).value

        val statement =
            ifStatement(keyword, condition, thenBranch.statements, elseBranch.statements, elseBranch.close)
        return Result.Success(Parsed(statement, afterElse))
    }

    private fun ifStatement(
        keyword: Token,
        condition: Expression,
        thenBranch: List<Statement>,
        elseBranch: List<Statement>?,
        close: Token,
    ): Statement =
        IfStatement(
            condition = condition,
            thenBranch = thenBranch,
            elseBranch = elseBranch,
            range = Range(keyword.range.start, close.range.end),
        )
}
