package org.printscript.analyzer.versions

import org.printscript.analyzer.Analyzer
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.engine.PrintScriptAnalyzer
import org.printscript.analyzer.engine.Rule
import org.printscript.analyzer.rules.IdentifierNamingRule
import org.printscript.analyzer.rules.printlnArgumentRule

object PrintScript10 {
    fun analyzer(config: AnalyzerConfig): Analyzer = PrintScriptAnalyzer(rules(config))

    internal fun rules(config: AnalyzerConfig): List<Rule> =
        listOfNotNull(
            config.namingConvention?.let { IdentifierNamingRule(it) },
            printlnArgumentRule().takeIf { config.restrictPrintlnArguments },
        )
}
