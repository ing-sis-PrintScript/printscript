package org.printscript.analyzer.engine

import org.printscript.analyzer.Analyzer
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.ast.AssignmentStatement
import org.printscript.ast.AstNode
import org.printscript.ast.BinaryExpression
import org.printscript.ast.BooleanLiteral
import org.printscript.ast.CallExpression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.IfStatement
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.ast.UnaryExpression
import org.printscript.ast.VariableDeclaration
import org.printscript.common.PrintScriptError
import org.printscript.common.Result

internal class PrintScriptAnalyzer(
    private val rules: List<Rule>,
) : Analyzer {
    override fun analyze(
        program: Sequence<Result<AstNode, PrintScriptError>>,
        emit: DiagnosticEmitter,
    ) {
        program.forEach { element ->
            if (element is Result.Success) visit(element.value, emit)
        }
    }

    private fun visit(
        node: AstNode,
        emitter: DiagnosticEmitter,
    ) {
        rules.forEach { it.check(node, emitter) }
        children(node).forEach { visit(it, emitter) }
    }

    private fun children(node: AstNode): List<AstNode> =
        when (node) {
            is VariableDeclaration -> listOfNotNull(node.identifier, node.initializer)
            is AssignmentStatement -> listOf(node.target, node.value)
            is ExpressionStatement -> listOf(node.expression)
            is BinaryExpression -> listOf(node.left, node.right)
            is UnaryExpression -> listOf(node.operand)
            is CallExpression -> listOf(node.callee) + node.arguments

            is IfStatement -> listOf(node.condition) + node.thenBranch + node.elseBranch.orEmpty()
            is NumberLiteral, is StringLiteral, is BooleanLiteral, is Identifier -> emptyList()
        }
}
