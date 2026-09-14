package org.printscript.analyzer.config

data class AnalyzerConfig(
    val namingConvention: NamingConvention? = null,
    val restrictPrintlnArguments: Boolean = false,
    val restrictReadInputArguments: Boolean = false,
)
