package org.printscript.parser.statements

import org.printscript.ast.DeclarationKind
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
    // El mismo parser sirve para las dos versiones y no necesita saber en cual esta:
    // si ve un CONST es porque el lexer era el de 1.1. En 1.0 la palabra "const" no
    // esta en el mapa de keywords y sale IDENTIFIER, asi que nunca llega aca.
    override fun canHandle(type: TokenType): Boolean = type == TokenType.LET || type == TokenType.CONST

    override fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> {
        val keywordResult = stream.next()
        if (keywordResult is Result.Failure) return keywordResult
        val (keyword, afterKeyword) = (keywordResult as Result.Success).value

        val identifierResult = parseIdentifier(afterKeyword)
        if (identifierResult is Result.Failure) return identifierResult
        val (identifier, afterIdentifier) = (identifierResult as Result.Success).value

        val kind = if (keyword.type == TokenType.CONST) DeclarationKind.CONST else DeclarationKind.LET
        return finishDeclaration(keyword.range.start, identifier, afterIdentifier, kind)
    }

    private fun finishDeclaration(
        start: Position,
        identifier: Identifier,
        stream: TokenStream,
        kind: DeclarationKind,
    ): Result<Parsed<Statement>, PrintScriptError> {
        val typeResult = parseTypeAnnotation(stream)
        if (typeResult is Result.Failure) return typeResult
        val (declaredType, afterType) = (typeResult as Result.Success).value

        val initializerResult = parseInitializer(afterType, kind, identifier.range)
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
                kind = kind,
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
            TokenType.TYPE_BOOLEAN -> Result.Success(Parsed(DeclaredType.BOOLEAN, afterType))
            // Nombra lo que vino en vez de listar los validos: cuales existen depende de
            // la version, y este parser es el mismo para las dos. Ademas apunta al
            // problema real en vez de hacer que el usuario compare contra una lista.
            else -> Result.Failure(SyntaxError("'${token.value}' no es un tipo", token.range))
        }
    }

    // La gramática dice ["=", expression]: sin "=" no hay inicializador y el stream queda
    // donde estaba.
    //
    // La excepción es la constante: sin valor no se puede leer --nunca se inicializó-- ni
    // escribir --es constante--, así que queda inservible. Se corta al parsear y no al
    // ejecutar, y el error apunta al nombre de la variable.
    private fun parseInitializer(
        stream: TokenStream,
        kind: DeclarationKind,
        identifierRange: Range,
    ): Result<Parsed<Expression?>, PrintScriptError> {
        if (!stream.peekIs(TokenType.ASSIGN)) {
            if (kind == DeclarationKind.CONST) {
                return Result.Failure(
                    SyntaxError("Una constante tiene que declararse con un valor", identifierRange),
                )
            }
            return Result.Success(Parsed(null, stream))
        }

        val assignResult = stream.skip(TokenType.ASSIGN)
        if (assignResult is Result.Failure) return assignResult
        return expressions.parse((assignResult as Result.Success).value)
    }
}
