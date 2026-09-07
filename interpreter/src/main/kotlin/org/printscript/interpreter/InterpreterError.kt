package org.printscript.interpreter

import org.printscript.common.PrintScriptError
import org.printscript.common.Range

data class InterpreterError(
    override val message: String,
    override val range: Range,
) : PrintScriptError {
    override fun toString() = "Error de ejecución en $range: $message"
}
