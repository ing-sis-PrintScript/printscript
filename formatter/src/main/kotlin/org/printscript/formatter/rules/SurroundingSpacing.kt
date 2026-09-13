package org.printscript.formatter.rules

import org.printscript.formatter.config.Spacing
import org.printscript.token.Token
import org.printscript.token.TokenType

class SurroundingSpacing(
    private val types: Set<TokenType>,
    private val before: Spacing? = null,
    private val after: Spacing? = null,
) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? =
        when {
            current.type in types -> before?.render()
            prev?.type in types -> after?.render()
            else -> null
        }
}

private val OPERATORS = setOf(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH)

private val COLON = setOf(TokenType.COLON)

private val ASSIGN = setOf(TokenType.ASSIGN)

fun colonSpacing(
    before: Spacing?,
    after: Spacing?,
): SpacingRule = SurroundingSpacing(COLON, before, after)

fun assignmentSpacing(around: Spacing?): SpacingRule = SurroundingSpacing(ASSIGN, around, around)

fun operatorSpacing(mandatory: Boolean): SpacingRule {
    val spacing = if (mandatory) Spacing.SINGLE else null
    return SurroundingSpacing(OPERATORS, spacing, spacing)
}
