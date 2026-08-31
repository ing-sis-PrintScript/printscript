package org.printscript.formatter.statements

import org.printscript.ast.ASTNode
import org.printscript.ast.AssignmentStatement
import org.printscript.ast.Expression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.VariableDeclaration
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.engine.NodeDispatcher
import org.printscript.formatter.engine.NodeFormatter
import org.printscript.formatter.syntax.Syntax

class PrintScript10StatementDispatcher(
    private val declarations: NodeFormatter<VariableDeclaration>,
    private val expressions: NodeFormatter<Expression>,
) : NodeDispatcher {
    override fun formatOrNull(
        node: ASTNode,
        context: FormatterContext,
    ): FormattedCode? =
        when (node) {
            is VariableDeclaration -> declarations.format(node, context)
            is AssignmentStatement -> formatAssignment(node, context)
            is ExpressionStatement -> formatExpressionStatement(node, context)
            else -> null
        }

    private fun formatAssignment(
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

    private fun formatExpressionStatement(
        node: ExpressionStatement,
        context: FormatterContext,
    ): FormattedCode = expressions.format(node.expression, context) + FormattedCode(Syntax.TERMINATOR)
}
