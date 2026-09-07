package org.printscript.formatter.syntax

import org.printscript.ast.ASTNode
import org.printscript.ast.CallExpression
import org.printscript.ast.ExpressionStatement
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext

class StatementSeparator {
    fun before(
        isFirst: Boolean,
        node: ASTNode,
        context: FormatterContext,
    ): FormattedCode =
        when {
            isFirst -> FormattedCode.EMPTY
            isPrintln(node) -> FormattedCode(context.config.blankLinesBeforePrintln.render())
            else -> FormattedCode.EMPTY
        }

    fun after(): FormattedCode = FormattedCode(Syntax.STATEMENT_SEPARATOR)

    private fun isPrintln(node: ASTNode): Boolean {
        val expression = (node as? ExpressionStatement)?.expression
        return expression is CallExpression && expression.callee.name == Syntax.PRINTLN
    }
}
