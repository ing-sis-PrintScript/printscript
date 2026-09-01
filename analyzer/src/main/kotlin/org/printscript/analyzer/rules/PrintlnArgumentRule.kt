package org.printscript.analyzer.rules

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.Severity
import org.printscript.analyzer.engine.Rule
import org.printscript.ast.ASTNode
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral

private const val RULE_ID = "println-argument"
private const val PRINTLN = "println"

/**
 * println solo admite un identificador o un literal como argumento — nunca
 * una expresión que haya que evaluar primero. Recorre TODOS los argumentos
 * de la llamada, no corta en el primero que falla, por si algún día println
 * admite más de uno.
 */
internal class PrintlnArgumentRule : Rule {
    override fun check(
        node: ASTNode,
        emitter: DiagnosticEmitter,
    ) {
        if (node !is CallExpression || node.callee.name != PRINTLN) return

        node.arguments
            .filterNot(::isIdentifierOrLiteral)
            .forEach { argument -> emitter.emit(violation(argument)) }
    }

    private fun isIdentifierOrLiteral(expression: Expression): Boolean =
        expression is Identifier || expression is NumberLiteral || expression is StringLiteral

    private fun violation(argument: Expression) =
        Diagnostic(
            rule = RULE_ID,
            message = "println solo admite un identificador o un literal, no una expresión.",
            range = argument.range,
            severity = Severity.ERROR,
        )
}
