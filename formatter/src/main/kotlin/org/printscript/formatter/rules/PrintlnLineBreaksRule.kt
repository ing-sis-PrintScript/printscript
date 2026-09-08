package org.printscript.formatter.rules

import org.printscript.formatter.config.BlankLines
import org.printscript.token.Token
import org.printscript.token.TokenType

// Cuantas lineas en blanco van antes de un println.
//
// El salto que separa una sentencia de la siguiente va siempre, y las lineas en blanco
// se suman arriba: por eso es un "\n" mas los que pida la config.
//
// Si el println es lo primero del archivo no hay nada que separar, y prev es null.
class PrintlnLineBreaksRule(private val blankLines: BlankLines? = null) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
    ): String? {
        if (prev == null || current.type != TokenType.PRINTLN) return null

        return blankLines?.let { "\n" + it.render() }
    }
}
