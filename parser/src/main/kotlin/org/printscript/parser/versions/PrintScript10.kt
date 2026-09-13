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

    internal fun statementParsers(expressions: ExpressionParser): List<StatementParser> =
        listOf(
            DeclarationParser(expressions),
            AssignmentParser(expressions),
            CallParser(expressions),
        )

    fun parser(): Parser {
        val parsers = statementParsers()
        return Parser(StatementParsers { parsers }, SkipToSemicolon)
    }
}
