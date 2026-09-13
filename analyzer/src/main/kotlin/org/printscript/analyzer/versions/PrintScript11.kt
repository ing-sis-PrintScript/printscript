package org.printscript.analyzer.versions

import org.printscript.analyzer.Analyzer
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.engine.PrintScriptAnalyzer
import org.printscript.analyzer.engine.Rule
import org.printscript.analyzer.rules.readInputArgumentRule

object PrintScript11 {
    fun analyzer(config: AnalyzerConfig): Analyzer = PrintScriptAnalyzer(rules(config))

    private fun rules(config: AnalyzerConfig): List<Rule> =
        PrintScript10.rules(config) +
            listOfNotNull(readInputArgumentRule().takeIf { config.restrictReadInputArguments })
}
