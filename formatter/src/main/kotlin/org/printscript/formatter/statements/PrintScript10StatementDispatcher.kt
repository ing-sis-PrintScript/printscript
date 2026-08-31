package org.printscript.formatter.statements

import org.printscript.ast.ASTNode
import org.printscript.ast.AssignmentStatement
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.VariableDeclaration
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.NodeFormatter
import org.printscript.formatter.PartialNodeFormatter

class PrintScript10StatementDispatcher(
    private val declarations: NodeFormatter<VariableDeclaration>,
    private val assignments: NodeFormatter<AssignmentStatement>,
    private val expressionStatements: NodeFormatter<ExpressionStatement>,
) : PartialNodeFormatter<ASTNode> {
    override fun formatOrNull(
        node: ASTNode,
        context: FormatterContext,
    ): FormattedCode? =
        when (node) {
            is VariableDeclaration -> declarations.format(node, context)
            is AssignmentStatement -> assignments.format(node, context)
            is ExpressionStatement -> expressionStatements.format(node, context)
            else -> null
        }
}
