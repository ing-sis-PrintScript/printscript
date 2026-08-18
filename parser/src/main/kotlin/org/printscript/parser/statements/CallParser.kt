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

/**
 * call = "println", "(", expression, ")", ";" ;
 *
 * Produce DOS nodos anidados, no uno:
 *
 *   ExpressionStatement        ← la línea del programa
 *   └── CallExpression         ← la llamada, que es una Expression
 *
 * Parece redundante para 1.0, donde println siempre aparece solo en su línea.
 * Pero en 1.1 llega readInput, que va a aparecer como "let x: string =
 * readInput(...)" — o sea, una CallExpression en posición de valor. Al tenerla
 * como Expression, ese caso entra sin tocar nada: cambia dónde cuelga, no qué es.
 *
 * El ExpressionStatement es el que dice "esta expresión va sola acá y su valor
 * no lo usa nadie".
 */
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