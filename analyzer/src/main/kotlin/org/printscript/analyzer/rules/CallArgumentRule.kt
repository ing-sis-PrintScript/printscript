package org.printscript.analyzer.rules

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.Severity
import org.printscript.analyzer.engine.Rule
import org.printscript.ast.ASTNode
import org.printscript.ast.BooleanLiteral
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral

/**
 * El argumento de una llamada tiene que ser un identificador o un literal, nunca una
 * expresión que haya que evaluar primero.
 *
 * Es la forma que comparten mandatory-variable-or-literal-in-println (1.0) y
 * mandatory-variable-or-literal-in-readInput (1.1), así que va una sola vez y se
 * parametriza con el nombre de la función. Sumar otra es agregar una fábrica abajo, no
 * una clase nueva — mismo criterio que SurroundingSpacing en el formatter.
 *
 * Recorre TODOS los argumentos y no corta en el primero que falla, por si algún día
 * una de estas funciones admite más de uno.
 */
internal class CallArgumentRule(
    private val function: String,
    private val ruleId: String,
) : Rule {
    override fun check(
        node: ASTNode,
        emitter: DiagnosticEmitter,
    ) {
        if (node !is CallExpression || node.callee.name != function) return

        node.arguments
            .filterNot(::isIdentifierOrLiteral)
            .forEach { argument -> emitter.emit(violation(argument)) }
    }

    private fun isIdentifierOrLiteral(expression: Expression): Boolean =
        expression is Identifier ||
            expression is NumberLiteral ||
            expression is StringLiteral ||
            expression is BooleanLiteral

    private fun violation(argument: Expression) =
        Diagnostic(
            rule = ruleId,
            message = "$function solo admite un identificador o un literal, no una expresión.",
            range = argument.range,
            severity = Severity.ERROR,
        )
}

// mandatory-variable-or-literal-in-println
internal fun printlnArgumentRule(): Rule = CallArgumentRule("println", "println-argument")

// mandatory-variable-or-literal-in-readInput
internal fun readInputArgumentRule(): Rule = CallArgumentRule("readInput", "read-input-argument")
