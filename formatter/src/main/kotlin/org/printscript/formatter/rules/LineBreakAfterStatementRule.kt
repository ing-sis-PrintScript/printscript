package org.printscript.formatter.rules

import org.printscript.token.Token

// Un salto de linea despues de cada sentencia (mandatory-line-break-after-statement).
//
// El golden del TCK es la mejor muestra del modelo incremental: mete los saltos donde
// faltaban y no toca ninguna otra cosa --cada declaracion conserva su propio espaciado
// alrededor del ':' y del '='--.
class LineBreakAfterStatementRule(private val mandatory: Boolean = false) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? {
        if (!mandatory || !startsStatementAfterSemicolon(prev, current)) return null

        return "\n"
    }
}
