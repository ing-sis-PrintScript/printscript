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
import org.printscript.parser.TokenStream
import org.printscript.token.TokenType


class CallParser(
    private val expressions: ExpressionParser,
) : StatementParser {

    override fun canHandle(type: TokenType): Boolean = type == TokenType.PRINTLN

    override fun parse(stream: TokenStream): Result<Statement, PrintScriptError> =
        stream.expect(TokenType.PRINTLN, "'println'").flatMap { callee ->
            stream.skip(TokenType.LPAREN, "'(' después de println").flatMap {
                expressions.parse(stream).flatMap { argument ->
                    stream.expect(TokenType.RPAREN, "')' para cerrar la llamada").flatMap { rparen ->
                        stream.expect(TokenType.SEMICOLON, "';' al final de la sentencia")
                            .map { semicolon ->
                                val call = CallExpression(
                                    // El lexer emite PRINTLN como keyword propio, pero el AST
                                    // habla de conceptos del lenguaje: acá es un nombre invocado.
                                    callee = Identifier(callee.lexeme, callee.range),
                                    arguments = listOf(argument),
                                    range = Range(callee.range.start, rparen.range.end),
                                )
                                ExpressionStatement(
                                    expression = call,
                                    range = Range(callee.range.start, semicolon.range.end),
                                )
                            }
                    }
                }
            }
        }
}