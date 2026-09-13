package org.printscript.formatter.rules

import org.printscript.formatter.config.BracePosition
import org.printscript.token.Token
import org.printscript.token.TokenType

class IfBraceRule(private val position: BracePosition? = null) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? = if (current.type == TokenType.LBRACE) position?.render() else null
}
