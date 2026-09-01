package org.printscript.analyzer.rules

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.Severity
import org.printscript.analyzer.config.NamingConvention
import org.printscript.analyzer.engine.Rule
import org.printscript.ast.ASTNode
import org.printscript.ast.VariableDeclaration

private const val RULE_ID = "identifier-naming"

/**
 * El único lugar donde PrintScript 1.0 introduce un nombre nuevo es una
 * declaración (`let`): una asignación o un uso dentro de una expresión solo
 * referencian un nombre que ya existe. Revisar esos otros lugares de nuevo
 * no encontraría nada distinto — sería el mismo hallazgo repetido por cada
 * vez que se lee la variable, no un problema nuevo.
 */
internal class IdentifierNamingRule(
    private val convention: NamingConvention,
) : Rule {
    override fun check(
        node: ASTNode,
        emitter: DiagnosticEmitter,
    ) {
        if (node !is VariableDeclaration) return

        val name = node.identifier.name
        if (!convention.matches(name)) {
            emitter.emit(
                Diagnostic(
                    rule = RULE_ID,
                    message = "El identificador '$name' no sigue la convención de nombres configurada.",
                    range = node.identifier.range,
                    severity = Severity.WARNING,
                ),
            )
        }
    }
}
