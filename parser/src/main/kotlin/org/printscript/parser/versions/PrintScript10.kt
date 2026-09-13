package org.printscript.parser.versions

import org.printscript.parser.ExpressionParser
import org.printscript.parser.Parser
import org.printscript.parser.PrecedenceExpressionParser
import org.printscript.parser.SkipToSemicolon
import org.printscript.parser.statements.AssignmentParser
import org.printscript.parser.statements.CallParser
import org.printscript.parser.statements.DeclarationParser
import org.printscript.parser.statements.StatementParser
import org.printscript.parser.statements.StatementParsers

object PrintScript10 {
    fun statementParsers(): List<StatementParser> = statementParsers(PrecedenceExpressionParser())

    /**
     * Los statement parsers que las dos versiones comparten, armados sobre el
     * parser de expresiones que se les dé. Lo único que cambia entre versiones
     * es qué sabe parsear ese parser de expresiones (1.1 suma readInput y
     * readEnv), así que 1.1 reusa esta misma lista en vez de copiarla.
     */
    internal fun statementParsers(expressions: ExpressionParser): List<StatementParser> =
        listOf(
            DeclarationParser(expressions),
            AssignmentParser(expressions),
            CallParser(expressions),
        )

    fun parser(): Parser {
        // La lista se arma una sola vez y despues se consulta, no se rearma por
        // cada statement.
        val parsers = statementParsers()
        return Parser(StatementParsers { parsers }, SkipToSemicolon)
    }
}
