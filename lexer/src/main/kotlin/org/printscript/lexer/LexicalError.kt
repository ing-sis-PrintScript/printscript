package org.printscript.lexer

import org.printscript.common.PrintScriptError
import org.printscript.common.Range

/**
 * Antes era: class LexicalError(message: String, val range: Range) : Exception(message)
 * Ahora es un dato común y corriente. No hereda de Exception, así que no se puede tirar
 * con throw ni escapar sola: el único camino para que llegue a alguien es adentro de un Result.
 */
data class LexicalError(override val message: String, override val range: Range) : PrintScriptError {
    override fun toString() = "Error léxico en $range: $message"
}
