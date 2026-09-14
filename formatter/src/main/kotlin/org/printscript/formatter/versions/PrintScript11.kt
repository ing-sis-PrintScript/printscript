package org.printscript.formatter.versions

import org.printscript.formatter.Formatter
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.engine.TokenFormatter
import org.printscript.formatter.rules.IfBraceRule
import org.printscript.formatter.rules.SpacingMatcher
import org.printscript.formatter.rules.SpacingRule

object PrintScript11 {
    fun rules(config: FormatterConfig): List<SpacingRule> =
        listOf(IfBraceRule(config.ifBrace)) + PrintScript10.rules(config)

    fun formatter(config: FormatterConfig): Formatter =
        TokenFormatter(SpacingMatcher(rules(config)), config.indentInsideIf)
}
