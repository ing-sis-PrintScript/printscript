package org.printscript.parser.token

import org.printscript.common.PrintScriptError
import org.printscript.common.Range


data class UnexpectedEndOfInput(override val range: Range) : PrintScriptError {

    override val message: String = "Fin de archivo inesperado"

    override fun toString() = "Error de sintaxis en $range: $message"
}
