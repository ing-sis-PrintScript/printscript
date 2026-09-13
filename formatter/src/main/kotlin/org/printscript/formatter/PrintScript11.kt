package org.printscript.formatter

import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.engine.TokenFormatter
import org.printscript.formatter.rules.IfBraceRule
import org.printscript.formatter.rules.SpacingMatcher
import org.printscript.formatter.rules.SpacingRule

// Que reglas de formato trae PrintScript 1.1: las de 1.0 mas la de la llave.
//
// La de la llave va ADELANTE y no al final: mandatory-single-space-separation opina de
// TODOS los espacios, asi que si quedara detras le pisaria la llave. El criterio de la
// lista sigue siendo el mismo, de la mas especifica a la mas amplia.
//
// indent-inside-if no aparece aca porque no es una regla: es un paso del TokenFormatter.
object PrintScript11 {
    fun rules(config: FormatterConfig): List<SpacingRule> =
        listOf(IfBraceRule(config.ifBrace)) + PrintScript10.rules(config)

    fun formatter(config: FormatterConfig): Formatter =
        TokenFormatter(SpacingMatcher(rules(config)), config.indentInsideIf)
}
