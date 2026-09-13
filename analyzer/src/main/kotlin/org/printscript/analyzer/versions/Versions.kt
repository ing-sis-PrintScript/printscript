package org.printscript.analyzer.versions

import org.printscript.analyzer.Analyzer
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.common.Version

fun analyzerFor(
    version: Version,
    config: AnalyzerConfig,
): Analyzer =
    when (version) {
        Version.V10 -> PrintScript10.analyzer(config)
        Version.V11 -> PrintScript11.analyzer(config)
    }
