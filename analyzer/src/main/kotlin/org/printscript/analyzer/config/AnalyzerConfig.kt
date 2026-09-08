package org.printscript.analyzer.config

/**
 * Qué necesitan las reglas de estilo. El CLI arma esta instancia leyendo y parseando su
 * propio archivo de configuración — este módulo nunca toca el filesystem.
 *
 * Los defaults son APAGADO, no encendido: una regla que el archivo de configuración no
 * menciona no se aplica. Con la config vacía el analyzer no reporta nada, que es lo que
 * espera el caso valid-no-rules del TCK.
 */
data class AnalyzerConfig(
    val namingConvention: NamingConvention? = null,
    val restrictPrintlnArguments: Boolean = false,
    // La regla todavía no existe (es de 1.1), pero la clave ya llega en los configs y
    // el loader tiene que aceptarla sin fallar.
    val restrictReadInputArguments: Boolean = false,
)
