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
import org.printscript.common.flatMap
import org.printscript.common.map
import org.printscript.parser.ExpressionParser
import org.printscript.parser.SyntaxError
import org.printscript.parser.TokenStream
import org.printscript.token.TokenType


class DeclarationParser(
    private val expressions: ExpressionParser = ExpressionParser(),
) : StatementParser {

    override fun canHandle(type: TokenType): Boolean = type == TokenType.LET

    override fun parse(stream: TokenStream): Result<Statement, PrintScriptError> =
        stream.expect(TokenType.LET, "'let'").flatMap { letToken ->
            parseIdentifier(stream).flatMap { identifier ->
                parseTypeAnnotation(stream).flatMap { declaredType ->
                    parseInitializer(stream).flatMap { initializer ->
                        stream.expect(TokenType.SEMICOLON, "';' al final de la declaración")
                            .map { semicolon ->
                                VariableDeclaration(
                                    identifier = identifier,
                                    declaredType = declaredType,
                                    initializer = initializer,
                                    // De punta a punta: del "let" al ";".
                                    range = spanOf(letToken.range.start, semicolon.range.end),
                                )
                            }
                    }
                }
            }
        }

    private fun parseIdentifier(stream: TokenStream): Result<Identifier, PrintScriptError> =
        stream.expect(TokenType.IDENTIFIER, "el nombre de la variable")
            .map { Identifier(it.value, it.range) }


    private fun parseTypeAnnotation(stream: TokenStream): Result<DeclaredType, PrintScriptError> =
        stream.skip(TokenType.COLON, "':' antes del tipo").flatMap {
            stream.next().flatMap { token ->
                when (token.type) {
                    TokenType.TYPE_NUMBER -> Result.Success(DeclaredType.NUMBER)
                    TokenType.TYPE_STRING -> Result.Success(DeclaredType.STRING)
                    else -> Result.Failure(
                        SyntaxError("Se esperaba 'number' o 'string'", token.range),
                    )
                }
            }
        }


    private fun parseInitializer(stream: TokenStream): Result<Expression?, PrintScriptError> {
        if (!stream.peekIs(TokenType.ASSIGN)) return Result.Success(null)

        return stream.skip(TokenType.ASSIGN, "'='").flatMap {
            expressions.parse(stream).map { it }
        }
    }

    private fun spanOf(start: Position, end: Position) = Range(start, end)
}