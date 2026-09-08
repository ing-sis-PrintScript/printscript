package org.printscript.formatter

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
    // El ORDEN decide algo solo cuando dos reglas opinan del mismo espacio. Con los
    // configs del TCK no pasa --activan una clave por vez-- pero el criterio es: de la
    // mas especifica a la mas amplia.
    //
    // Las dos que escriben despues de un ';' se pisarian entre si, y la separacion por
    // un espacio opina de TODOS los espacios, por eso va ultima.
    fun rules(config: FormatterConfig): List<SpacingRule> =
        listOf(
            colonSpacing(config.spaceBeforeColon, config.spaceAfterColon),
            assignmentSpacing(config.spaceAroundAssignment),
            operatorSpacing(config.spaceSurroundingOperations),
            LineBreaksAfterPrintlnRule(config.lineBreaksAfterPrintln),
            LineBreakAfterStatementRule(config.lineBreakAfterStatement),
            SingleSpaceSeparationRule(config.singleSpaceSeparation),
        )

    fun formatter(config: FormatterConfig): Formatter = TokenFormatter(SpacingMatcher(rules(config)))
}
