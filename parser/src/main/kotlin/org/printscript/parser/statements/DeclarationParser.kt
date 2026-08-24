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
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.expect
import org.printscript.parser.token.next
import org.printscript.parser.token.peekIs
import org.printscript.parser.token.skip
import org.printscript.token.TokenType

/**
 * declaration = "let", identifier, ":", type, ["=", expression], ";" ;
 *
 * Cada paso recibe el stream que dejo el anterior y devuelve el suyo. La cadena
 * de flatMap corta sola en el primer Failure: no hay un solo "si fallo, salir"
 * escrito a mano.
 */
class DeclarationParser(
    private val expressions: ExpressionParser,
) : StatementParser {
    override fun canHandle(type: TokenType): Boolean = type == TokenType.LET

    override fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> =
        stream.expect(TokenType.LET).flatMap { (letToken, afterLet) ->
            parseIdentifier(afterLet).flatMap { (identifier, afterIdentifier) ->
                parseTypeAnnotation(afterIdentifier).flatMap { (declaredType, afterType) ->
                    parseInitializer(afterType).flatMap { (initializer, afterInitializer) ->
                        afterInitializer.expect(TokenType.SEMICOLON).map { (semicolon, afterSemicolon) ->
                            Parsed(
                                VariableDeclaration(
                                    identifier = identifier,
                                    declaredType = declaredType,
                                    initializer = initializer,
                                    // De punta a punta: del "let" al ";".
                                    range = spanOf(letToken.range.start, semicolon.range.end),
                                ),
                                afterSemicolon,
                            )
                        }
                    }
                }
            }
        }

    private fun parseIdentifier(stream: TokenStream): Result<Parsed<Identifier>, PrintScriptError> =
        stream.expect(TokenType.IDENTIFIER).map { (token, rest) ->
            Parsed(Identifier(token.value, token.range), rest)
        }

    private fun parseTypeAnnotation(stream: TokenStream): Result<Parsed<DeclaredType>, PrintScriptError> =
        stream.skip(TokenType.COLON).flatMap { afterColon ->
            afterColon.next().flatMap { (token, afterType) ->
                when (token.type) {
                    TokenType.TYPE_NUMBER -> Result.Success(Parsed(DeclaredType.NUMBER, afterType))
                    TokenType.TYPE_STRING -> Result.Success(Parsed(DeclaredType.STRING, afterType))
                    else ->
                        Result.Failure(
                            SyntaxError("Se esperaba 'number' o 'string'", token.range),
                        )
                }
            }
        }

    /**
     * La gramatica dice ["=", expression]: si no hay "=", no hay inicializador
     * y el stream sigue exactamente donde estaba.
     */
    private fun parseInitializer(stream: TokenStream): Result<Parsed<Expression?>, PrintScriptError> {
        if (!stream.peekIs(TokenType.ASSIGN)) return Result.Success(Parsed(null, stream))

        return stream.skip(TokenType.ASSIGN).flatMap { afterAssign ->
            expressions.parse(afterAssign)
        }
    }

    private fun spanOf(
        start: Position,
        end: Position,
    ) = Range(start, end)
}
