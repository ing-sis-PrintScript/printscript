package org.printscript.formatter.rules

import org.printscript.token.Token

class LineBreakAfterStatementRule(private val mandatory: Boolean = false) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? {
        if (!mandatory || !startsStatement(prev, current)) return null

        return "\n"
    }
}
