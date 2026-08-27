package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.expressions.PrintScript10ExpressionFormatter
import org.printscript.formatter.statements.AssignmentFormatter
import org.printscript.formatter.statements.DeclarationFormatter
import org.printscript.formatter.statements.ExpressionStatementFormatter
import org.printscript.formatter.statements.PrintScript10StatementDispatcher

object PrintScript10 {
    fun nodeFormatters(): List<PartialNodeFormatter<ASTNode>> {
        val expressions = PrintScript10ExpressionFormatter()
        return listOf(
            PrintScript10StatementDispatcher(
                declarations = DeclarationFormatter(expressions),
                assignments = AssignmentFormatter(expressions),
                expressionStatements = ExpressionStatementFormatter(expressions),
            ),
        )
    }

    fun formatter(config: FormatterConfig): Formatter =
        PrintScriptFormatter(nodeFormatters(), StatementSeparator(), config)
}
