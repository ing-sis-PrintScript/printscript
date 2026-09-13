package org.printscript.formatter.rules

import org.printscript.formatter.config.BlankLines
import org.printscript.token.Token
import org.printscript.token.TokenType

class LineBreaksAfterPrintlnRule(private val blankLines: BlankLines? = null) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? {
        if (blankLines == null || state.lastStatementHead != TokenType.PRINTLN) return null
        if (!startsStatementAfterSemicolon(prev, current)) return null

        return "\n" + blankLines.render()
    }
}
