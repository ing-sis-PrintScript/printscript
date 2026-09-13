package org.printscript.analyzer

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.common.Version

// Que analyzer corresponde a cada version del lenguaje. Igual que en los otros modulos:
// la clase que recorre el arbol no sabe que existen versiones, recibe las reglas ya
// armadas.
fun analyzerFor(
    version: Version,
    config: AnalyzerConfig,
): Analyzer =
    when (version) {
        Version.V10 -> PrintScript10.analyzer(config)
        Version.V11 -> PrintScript11.analyzer(config)
    }
