package org.printscript.analyzer

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.engine.PrintScriptAnalyzer
import org.printscript.analyzer.engine.Rule
import org.printscript.analyzer.rules.IdentifierNamingRule
import org.printscript.analyzer.rules.PrintlnArgumentRule

/**
 * Qué reglas de estilo se aplican en PrintScript 1.0, armadas a partir de la
 * configuración recibida.
 *
 * Espejo del PrintScript10 del lexer/parser/interpreter/formatter: agregar una regla
 * nueva es un constructor más en rules(), no tocar Analyzer ni las reglas existentes.
 * Apagar una regla por configuración es sacarla de la lista, no un if adentro de la
 * regla misma — por eso una convención en null no arma la regla en vez de armar una que
 * no reporta nada.
 */
object PrintScript10 {
    fun analyzer(config: AnalyzerConfig): Analyzer = PrintScriptAnalyzer(rules(config))

    private fun rules(config: AnalyzerConfig): List<Rule> =
        listOfNotNull(
            config.namingConvention?.let { IdentifierNamingRule(it) },
            PrintlnArgumentRule().takeIf { config.restrictPrintlnArguments },
        )
}
