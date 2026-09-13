package org.printscript.formatter.rules

import org.printscript.formatter.config.BracePosition
import org.printscript.token.Token
import org.printscript.token.TokenType

// Donde va la "{" que abre un bloque (if-brace-same-line / if-brace-below-line).
//
// Una sola regla para las dos claves del TCK, porque las dos dicen lo mismo desde
// lados opuestos: la posicion ya llega resuelta en un BracePosition. Asi no hay dos
// reglas que puedan opinar del mismo espacio y el orden de la lista no decide nada.
//
// No pregunta por el if: en PrintScript una "{" solo puede estar abriendo un bloque.
//
// Cuando la llave va abajo devuelve solo el salto de linea; la sangria se la pone
// despues el formatter, que es quien sabe en que nivel esta.
class IfBraceRule(private val position: BracePosition? = null) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? = if (current.type == TokenType.LBRACE) position?.render() else null
}
