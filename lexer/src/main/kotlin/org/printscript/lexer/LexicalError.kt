package org.printscript.lexer

import org.printscript.common.PrintScriptError
import org.printscript.common.Range

data class LexicalError(override val message: String, override val range: Range) : PrintScriptError {
    override fun toString() = "Error léxico en $range: $message"
}
