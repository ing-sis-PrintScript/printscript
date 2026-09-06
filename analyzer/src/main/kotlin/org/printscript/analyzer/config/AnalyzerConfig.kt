package org.printscript.analyzer.config

/**
 * Qué necesitan las dos reglas de 1.0. El CLI arma esta instancia leyendo
 * y parseando su propio archivo de configuración (YAML/JSON, lo que sea) —
 * este módulo nunca toca el filesystem, así que solo conoce el resultado ya
 * construido, no de dónde salió.
 */
data class AnalyzerConfig(
    val namingConvention: NamingConvention = CamelCase,
    val restrictPrintlnArguments: Boolean = true,
)
