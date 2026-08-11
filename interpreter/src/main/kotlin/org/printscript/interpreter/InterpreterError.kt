package org.printscript.interpreter

import org.printscript.common.Range

data class InterpreterError(
    val message: String,
    val range: Range
) {
    override fun toString() = "Error de ejecución en $range: $message"
}

internal class RuntimeError(message: String, val range: Range) : Exception(message)