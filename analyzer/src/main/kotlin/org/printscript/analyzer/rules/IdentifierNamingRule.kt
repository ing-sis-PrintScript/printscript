package org.printscript.analyzer.rules

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.Severity
import org.printscript.analyzer.config.NamingConvention
import org.printscript.analyzer.engine.Rule
import org.printscript.ast.AstNode
import org.printscript.ast.VariableDeclaration

private const val RULE_ID = "identifier-naming"

internal class IdentifierNamingRule(
    private val convention: NamingConvention,
) : Rule {
    override fun check(
        node: AstNode,
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
