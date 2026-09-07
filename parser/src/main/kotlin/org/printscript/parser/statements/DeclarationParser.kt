package org.printscript.parser.statements

import org.printscript.ast.DeclaredType
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.Statement
import org.printscript.ast.VariableDeclaration
import org.printscript.common.Position
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.map
import org.printscript.parser.ExpressionParser
import org.printscript.parser.SyntaxError
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.expect
import org.printscript.parser.token.next
import org.printscript.parser.token.peekIs
import org.printscript.parser.token.skip
import org.printscript.token.TokenType

class DeclarationParser(
    private val expressions: ExpressionParser,
) : StatementParser {
    override fun canHandle(type: TokenType): Boolean = type == TokenType.LET

    override fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> {
        val letResult = stream.expect(TokenType.LET)
        if (letResult is Result.Failure) return letResult
        val (letToken, afterLet) = (letResult as Result.Success).value

        val identifierResult = parseIdentifier(afterLet)
        if (identifierResult is Result.Failure) return identifierResult
        val (identifier, afterIdentifier) = (identifierResult as Result.Success).value

        return finishDeclaration(letToken.range.start, identifier, afterIdentifier)
    }

    private fun finishDeclaration(
        start: Position,
        identifier: Identifier,
        stream: TokenStream,
    ): Result<Parsed<Statement>, PrintScriptError> {
        val typeResult = parseTypeAnnotation(stream)
        if (typeResult is Result.Failure) return typeResult
        val (declaredType, afterType) = (typeResult as Result.Success).value

        val initializerResult = parseInitializer(afterType)
        if (initializerResult is Result.Failure) return initializerResult
        val (initializer, afterInitializer) = (initializerResult as Result.Success).value

        val semicolonResult = afterInitializer.expect(TokenType.SEMICOLON, "al final de la declaración")
        if (semicolonResult is Result.Failure) return semicolonResult
        val (semicolon, afterSemicolon) = (semicolonResult as Result.Success).value

        val declaration =
            VariableDeclaration(
                identifier = identifier,
                declaredType = declaredType,
                initializer = initializer,
                range = Range(start, semicolon.range.end),
            )
        return Result.Success(Parsed(declaration, afterSemicolon))
    }

    private fun parseIdentifier(stream: TokenStream): Result<Parsed<Identifier>, PrintScriptError> =
        stream.expect(TokenType.IDENTIFIER, "como nombre de la variable").map { (token, rest) ->
            Parsed(Identifier(token.value, token.range), rest)
        }

    private fun parseTypeAnnotation(stream: TokenStream): Result<Parsed<DeclaredType>, PrintScriptError> {
        val colonResult = stream.skip(TokenType.COLON, "antes del tipo")
        if (colonResult is Result.Failure) return colonResult
        val afterColon = (colonResult as Result.Success).value

        val tokenResult = afterColon.next()
        if (tokenResult is Result.Failure) return tokenResult
        val (token, afterType) = (tokenResult as Result.Success).value

        return when (token.type) {
            TokenType.TYPE_NUMBER -> Result.Success(Parsed(DeclaredType.NUMBER, afterType))
            TokenType.TYPE_STRING -> Result.Success(Parsed(DeclaredType.STRING, afterType))
            else -> Result.Failure(SyntaxError("Se esperaba 'number' o 'string'", token.range))
        }
    }

    // La gramática dice ["=", expression]: sin "=" no hay inicializador y el stream queda donde estaba.
    private fun parseInitializer(stream: TokenStream): Result<Parsed<Expression?>, PrintScriptError> {
        if (!stream.peekIs(TokenType.ASSIGN)) return Result.Success(Parsed(null, stream))

        val assignResult = stream.skip(TokenType.ASSIGN)
        if (assignResult is Result.Failure) return assignResult
        return expressions.parse((assignResult as Result.Success).value)
    }
}
