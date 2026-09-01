package org.printscript.analyzer

import org.printscript.common.Range

/** Un hallazgo de una regla de estilo, con dónde ocurrió exactamente. */
data class Diagnostic(
    val rule: String,
    val message: String,
    val range: Range,
    val severity: Severity,
)

enum class Severity { ERROR, WARNING }

/**
 * A quién se le avisa cada Diagnostic, uno por vez, a medida que aparece.
 *
 * Un linter reporta TODOS los hallazgos de un archivo, no el primero — por
 * eso Analyzer.analyze() no devuelve un Result ni una List<Diagnostic>: eso
 * cortaría en el primer problema (Result) o exigiría tener el archivo entero
 * en memoria antes de devolver algo (List). Con un emitter, cada hallazgo
 * sale apenas se encuentra.
 */
fun interface DiagnosticEmitter {
    fun emit(diagnostic: Diagnostic)
}
