package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.ExpressionStatement

class PrintlnRecognizer {
    fun matches(node: ASTNode): Boolean = node is ExpressionStatement && isPrintlnCall(node.expression)

    private fun isPrintlnCall(expression: Expression): Boolean =
        expression is CallExpression && expression.callee.name == Syntax.PRINTLN
}
