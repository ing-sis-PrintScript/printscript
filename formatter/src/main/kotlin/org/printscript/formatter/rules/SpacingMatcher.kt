package org.printscript.formatter.rules

import org.printscript.token.Token

// Le pregunta a cada regla hasta que una conteste. La primera gana.
//
// OJO con la diferencia contra el TokenMatcher del lexer: alla las reglas son
// excluyentes por construccion --un caracter es digito, o letra, o simbolo, nunca dos--.
// Aca no lo son: dos reglas podrian opinar del mismo espacio. Pasa que los configs del
// TCK activan una sola regla por vez, asi que el conflicto no aparece. Si algun dia
// llegan dos claves juntas, el ORDEN de esta lista es la respuesta.
data class SpacingMatcher(private val rules: List<SpacingRule> = emptyList()) {
    fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? = rules.firstNotNullOfOrNull { it.spacingFor(prev, current, state) }
}
