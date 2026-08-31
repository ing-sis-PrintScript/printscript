package org.printscript.formatter.statements

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.Expression
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.NodeFormatter
import org.printscript.formatter.Syntax

class AssignmentFormatter(
    private val expressions: NodeFormatter<Expression>,
) : NodeFormatter<AssignmentStatement> {
    override fun format(
        node: AssignmentStatement,
        context: FormatterContext,
    ): FormattedCode {
        val spacing = FormattedCode(context.config.spaceAroundAssignment.render())
        return FormattedCode(node.target.name) +
            spacing +
            FormattedCode(Syntax.ASSIGN) +
            spacing +
            expressions.format(node.value, context) +
            FormattedCode(Syntax.TERMINATOR)
    }
}
