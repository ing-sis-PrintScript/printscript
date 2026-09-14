package org.printscript.formatter.rules

import org.printscript.token.Token

fun interface SpacingRule {
    fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String?
}
