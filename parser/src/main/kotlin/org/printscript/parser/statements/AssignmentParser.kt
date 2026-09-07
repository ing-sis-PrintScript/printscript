package org.printscript.parser.statements

import org.printscript.ast.AssignmentStatement
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

class AssignmentParser(
    private val expressions: ExpressionParser,
) : StatementParser {
    override fun canHandle(type: TokenType): Boolean = type == TokenType.IDENTIFIER

    // El identificador de la izquierda es el destino, no un valor: no lo parsea el ExpressionParser.
    override fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> {
        val nameResult = stream.expect(TokenType.IDENTIFIER, "como nombre de la variable")
        if (nameResult is Result.Failure) return nameResult
        val (name, afterName) = (nameResult as Result.Success).value

        val assignResult = afterName.skip(TokenType.ASSIGN, "en la asignación")
        if (assignResult is Result.Failure) return assignResult
        val afterAssign = (assignResult as Result.Success).value

        return parseValue(name, afterAssign)
    }

    private fun parseValue(
        name: Token,
        stream: TokenStream,
    ): Result<Parsed<Statement>, PrintScriptError> {
        val valueResult = expressions.parse(stream)
        if (valueResult is Result.Failure) return valueResult
        val (value, afterValue) = (valueResult as Result.Success).value

        val semicolonResult = afterValue.expect(TokenType.SEMICOLON, "al final de la asignación")
        if (semicolonResult is Result.Failure) return semicolonResult
        val (semicolon, afterSemicolon) = (semicolonResult as Result.Success).value

        val statement =
            AssignmentStatement(
                target = Identifier(name.value, name.range),
                value = value,
                range = Range(name.range.start, semicolon.range.end),
            )
        return Result.Success(Parsed(statement, afterSemicolon))
    }
}
