package org.printscript.analyzer

import org.printscript.common.Range

data class Diagnostic(
    val rule: String,
    val message: String,
    val range: Range,
    val severity: Severity,
)

enum class Severity { ERROR, WARNING }

fun interface DiagnosticEmitter {
    fun emit(diagnostic: Diagnostic)
}
