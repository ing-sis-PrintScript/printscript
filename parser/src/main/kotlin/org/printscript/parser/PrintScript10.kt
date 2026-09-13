package org.printscript.parser

import org.printscript.parser.statements.AssignmentParser
import org.printscript.parser.statements.CallParser
import org.printscript.parser.statements.DeclarationParser
import org.printscript.parser.statements.StatementParser
import org.printscript.parser.statements.StatementParsers

object PrintScript10 {
    fun statementParsers(): List<StatementParser> {
        val expressions = PrintScript10ExpressionParser()
        return listOf(
            DeclarationParser(expressions),
            AssignmentParser(expressions),
            CallParser(expressions),
        )
    }

    fun parser(): Parser {
        // La lista se arma una sola vez y despues se consulta, no se rearma por
        // cada statement.
        val parsers = statementParsers()
        return Parser(StatementParsers { parsers }, SkipToSemicolon)
    }
}
