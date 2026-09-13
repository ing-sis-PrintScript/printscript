package org.printscript.formatter.rules

import org.printscript.token.Token

data class SpacingMatcher(private val rules: List<SpacingRule> = emptyList()) {
    fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? = rules.firstNotNullOfOrNull { it.spacingFor(prev, current, state) }
}
