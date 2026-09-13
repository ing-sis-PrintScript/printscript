package org.printscript.formatter.rules

import org.printscript.token.Token
import org.printscript.token.TokenType

class SingleSpaceSeparationRule(private val mandatory: Boolean = false) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? {
        if (!mandatory || prev == null) return null
        if (current.type == TokenType.SEMICOLON || current.type == TokenType.EOF) return ""
        if (state.pendingWhitespace.contains('\n')) return null

        return " "
    }
}
