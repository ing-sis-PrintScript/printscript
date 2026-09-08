package org.printscript.formatter.rules

import org.printscript.formatter.config.Spacing
import org.printscript.token.Token
import org.printscript.token.TokenType

// El espacio de los dos lados del '=', que el TCK manda como
// enforce-spacing-around-equals (y su opuesto, enforce-no-spacing-around-equals).
// Una sola clave gobierna los dos lados, por eso alcanza un Spacing.
class AssignmentSpacingRule(private val around: Spacing? = null) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
    ): String? =
        when {
            current.type == TokenType.ASSIGN -> around?.render()
            prev?.type == TokenType.ASSIGN -> around?.render()
            else -> null
        }
}
