package org.printscript.parser.statements

import org.printscript.ast.AssignmentStatement
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

/**
 * assignment = identifier, "=", expression, ";" ;
 *
 * Sin "let" y sin tipo: la variable ya existe y su tipo quedó fijado en la
 * declaración. Que efectivamente exista es problema del análisis semántico,
 * no del parser.
 *
 * El identificador de la izquierda NO lo parsea el ExpressionParser: acá no es
 * un valor que se lee, es el destino de la asignación. La expresión de la
 * derecha sí, porque eso sí se evalúa.
 */
class AssignmentParser(
    private val expressions: ExpressionParser,
) : StatementParser {
    override fun canHandle(type: TokenType): Boolean = type == TokenType.IDENTIFIER

    override fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> =
        stream.expect(TokenType.IDENTIFIER).flatMap { (name, afterName) ->
            afterName.skip(TokenType.ASSIGN).flatMap { afterAssign ->
                expressions.parse(afterAssign).flatMap { (value, afterValue) ->
                    afterValue.expect(TokenType.SEMICOLON).map { (semicolon, afterSemicolon) ->
                        Parsed(
                            AssignmentStatement(
                                target = Identifier(name.value, name.range),
                                value = value,
                                range = Range(name.range.start, semicolon.range.end),
                            ),
                            afterSemicolon,
                        )
                    }
                }
            }
        }
}
