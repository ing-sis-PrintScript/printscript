package org.printscript.analyzer.engine

import org.printscript.analyzer.Analyzer
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.ast.ASTNode
import org.printscript.ast.AssignmentStatement
import org.printscript.ast.BinaryExpression
import org.printscript.ast.CallExpression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
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
        program: Sequence<Result<ASTNode, PrintScriptError>>,
        emit: DiagnosticEmitter,
    ) {
        program.forEach { element ->
            if (element is Result.Success) visit(element.value, emit)
        }
    }

    /** Aplica todas las reglas al nodo y después baja a sus hijos, sin acumular nada. */
    private fun visit(
        node: ASTNode,
        emitter: DiagnosticEmitter,
    ) {
        rules.forEach { it.check(node, emitter) }
        children(node).forEach { visit(it, emitter) }
    }

    /**
     * Los hijos directos de un nodo, para bajar recursivamente.
     *
     * A propósito exhaustivo sobre ASTNode: si 1.1 suma un nodo nuevo, esto
     * no compila hasta que alguien decida cómo bajar por él. Es lo opuesto
     * de Rule a propósito — el árbol que se recorre es cerrado (lo define
     * :ast), la lista de reglas que lo revisan es abierta.
     */
    private fun children(node: ASTNode): List<ASTNode> =
        when (node) {
            is VariableDeclaration -> listOfNotNull(node.identifier, node.initializer)
            is AssignmentStatement -> listOf(node.target, node.value)
            is ExpressionStatement -> listOf(node.expression)
            is BinaryExpression -> listOf(node.left, node.right)
            is UnaryExpression -> listOf(node.operand)
            is CallExpression -> listOf(node.callee) + node.arguments
            is NumberLiteral, is StringLiteral, is Identifier -> emptyList()
        }
}
