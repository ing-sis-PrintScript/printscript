package org.printscript.formatter.statements

import org.printscript.ast.Expression
import org.printscript.ast.ExpressionStatement
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.NodeFormatter
import org.printscript.formatter.Syntax

class ExpressionStatementFormatter(
    private val expressions: NodeFormatter<Expression>,
) : NodeFormatter<ExpressionStatement> {
    override fun format(
        node: ExpressionStatement,
        context: FormatterContext,
    ): FormattedCode = expressions.format(node.expression, context) + FormattedCode(Syntax.TERMINATOR)
}
