package org.printscript.formatter.versions

import org.printscript.formatter.Formatter
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.engine.TokenFormatter
import org.printscript.formatter.rules.LineBreakAfterStatementRule
import org.printscript.formatter.rules.LineBreaksAfterPrintlnRule
import org.printscript.formatter.rules.SingleSpaceSeparationRule
import org.printscript.formatter.rules.SpacingMatcher
import org.printscript.formatter.rules.SpacingRule
import org.printscript.formatter.rules.assignmentSpacing
import org.printscript.formatter.rules.colonSpacing
import org.printscript.formatter.rules.operatorSpacing

object PrintScript10 {
    fun rules(config: FormatterConfig): List<SpacingRule> =
        listOf(
            colonSpacing(config.spaceBeforeColon, config.spaceAfterColon),
            assignmentSpacing(config.spaceAroundAssignment),
            operatorSpacing(config.spaceSurroundingOperations),
            LineBreaksAfterPrintlnRule(config.lineBreaksAfterPrintln),
            LineBreakAfterStatementRule(config.lineBreakAfterStatement),
            SingleSpaceSeparationRule(config.singleSpaceSeparation),
        )

    fun formatter(config: FormatterConfig): Formatter =
        TokenFormatter(SpacingMatcher(rules(config)), config.indentInsideIf)
}
