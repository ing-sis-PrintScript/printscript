package org.printscript.parser

import org.printscript.parser.statements.AssignmentParser
import org.printscript.parser.statements.CallParser
import org.printscript.parser.statements.DeclarationParser
import org.printscript.parser.statements.StatementParser

/**
 * Qué statements existen en PrintScript 1.0.
 *
 * Espejo del PrintScript10 del lexer, que hace lo mismo con las TokenRule.
 * Cuando salga la 1.1 se agrega un PrintScript11 con estos parsers más los
 * nuevos (IfParser, ConstDeclarationParser...) y ni el Parser ni los parsers
 * existentes se tocan: eso es Open/Closed.
 *
 * Los tres comparten el mismo ExpressionParser porque no tiene estado: todo lo
 * que necesita viaja en el TokenStream que recibe por parámetro.
 *
 * A diferencia de las reglas del lexer, acá el orden NO importa: los tres
 * canHandle son mutuamente excluyentes (LET, IDENTIFIER, PRINTLN).
 */
object PrintScript10 {

    fun statementParsers(): List<StatementParser> {
        val expressions = ExpressionParser()
        return listOf(
            DeclarationParser(expressions),
            AssignmentParser(expressions),
            CallParser(expressions),
        )
    }
}