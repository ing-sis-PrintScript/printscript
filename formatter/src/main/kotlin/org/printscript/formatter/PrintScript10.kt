package org.printscript.formatter

import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.engine.TokenFormatter
import org.printscript.formatter.rules.AssignmentSpacingRule
import org.printscript.formatter.rules.ColonSpacingRule
import org.printscript.formatter.rules.PrintlnLineBreaksRule
import org.printscript.formatter.rules.SpacingMatcher
import org.printscript.formatter.rules.SpacingRule

object PrintScript10 {
    // El ORDEN de esta lista solo decide algo si dos reglas opinan del mismo espacio.
    // Con los configs del TCK no pasa nunca: activan una clave por vez. Si algun dia
    // llegan dos juntas, se resuelve aca.
    fun rules(config: FormatterConfig): List<SpacingRule> =
        listOf(
            ColonSpacingRule(config.spaceBeforeColon, config.spaceAfterColon),
            AssignmentSpacingRule(config.spaceAroundAssignment),
            PrintlnLineBreaksRule(config.blankLinesBeforePrintln),
        )

    fun formatter(config: FormatterConfig): Formatter = TokenFormatter(SpacingMatcher(rules(config)))
}
