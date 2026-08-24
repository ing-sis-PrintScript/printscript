package org.printscript.parser.statements

import org.printscript.ast.CallExpression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map
import org.printscript.parser.ExpressionParser
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.expect
import org.printscript.parser.token.skip
import org.printscript.token.TokenType

class CallParser(
    private val expressions: ExpressionParser,
) : StatementParser {
    override fun canHandle(type: TokenType): Boolean = type == TokenType.PRINTLN

    override fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> =
        stream.expect(TokenType.PRINTLN).flatMap { (callee, afterCallee) ->
            afterCallee.skip(TokenType.LPAREN, "después de println").flatMap { afterOpen ->
                expressions.parse(afterOpen).flatMap { (argument, afterArgument) ->
                    afterArgument.expect(TokenType.RPAREN, "para cerrar la llamada")
                        .flatMap { (rparen, afterClose) ->
                            afterClose.expect(TokenType.SEMICOLON, "al final de la sentencia")
                                .map { (semicolon, afterSemicolon) ->
                                    val call =
                                        CallExpression(
                                            // El nombre sale de `value`, igual que en los otros parsers.
                                            callee = Identifier(callee.value, callee.range),
                                            arguments = listOf(argument),
                                            range = Range(callee.range.start, rparen.range.end),
                                        )
                                    Parsed(
                                        ExpressionStatement(
                                            expression = call,
                                            // El statement incluye el ";", la llamada no.
                                            range = Range(callee.range.start, semicolon.range.end),
                                        ),
                                        afterSemicolon,
                                    )
                                }
                        }
                }
            }
        }
}
