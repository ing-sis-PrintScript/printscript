package org.printscript.formatter

import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.expressions.PrintScript10ExpressionFormatter
import org.printscript.formatter.statements.AssignmentFormatter
import org.printscript.formatter.statements.DeclarationFormatter
import org.printscript.formatter.statements.ExpressionStatementFormatter
import org.printscript.formatter.statements.PrintScript10StatementDispatcher

object PrintScript10 {
    fun formatter(config: FormatterConfig): Formatter {
        val expressions = PrintScript10ExpressionFormatter()
        val statements =
            PrintScript10StatementDispatcher(
                declarations = DeclarationFormatter(expressions),
                assignments = AssignmentFormatter(expressions),
                expressionStatements = ExpressionStatementFormatter(expressions),
            )
        return PrintScriptFormatter(statements, expressions, StatementSeparator(), config)
    }
}
