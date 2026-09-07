package org.printscript.parser

import org.printscript.common.PrintScriptError
import org.printscript.common.Range

data class SyntaxError(
    override val message: String,
    override val range: Range,
) : PrintScriptError {
    override fun toString() = "Error de sintaxis en $range: $message"
}
