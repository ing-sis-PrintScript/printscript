package org.printscript.analyzer.versions

import org.printscript.analyzer.Analyzer
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.engine.PrintScriptAnalyzer
import org.printscript.analyzer.engine.Rule
import org.printscript.analyzer.rules.readInputArgumentRule

/**
 * Qué reglas de estilo se aplican en PrintScript 1.1: las de 1.0 más la de readInput.
 *
 * El orden no importa: a diferencia de las del formatter, dos reglas del analyzer no
 * compiten por nada — cada una mira sus propios nodos y reporta por su cuenta.
 */
object PrintScript11 {
    fun analyzer(config: AnalyzerConfig): Analyzer = PrintScriptAnalyzer(rules(config))

    private fun rules(config: AnalyzerConfig): List<Rule> =
        PrintScript10.rules(config) +
            listOfNotNull(readInputArgumentRule().takeIf { config.restrictReadInputArguments })
}
