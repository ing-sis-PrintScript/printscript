package org.printscript.parser

import org.printscript.parser.statements.AssignmentParser
import org.printscript.parser.statements.CallParser
import org.printscript.parser.statements.DeclarationParser
import org.printscript.parser.statements.StatementParser

object PrintScript10 {

    fun statementParsers(): List<StatementParser> {
        val expressions = PrintScript10ExpressionParser()
        return listOf(
            DeclarationParser(expressions),
            AssignmentParser(expressions),
            CallParser(expressions),
        )
    }
}