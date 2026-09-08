package org.printscript.formatter.rules

import org.printscript.formatter.config.BlankLines
import org.printscript.token.Token
import org.printscript.token.TokenType

// Cuantas lineas en blanco quedan DESPUES de un println (line-breaks-after-println).
//
// Es DESPUES y no antes: los goldens ponen las lineas entre un println y lo que venga,
// no delante del println. Por eso se aplica en el primer token de la sentencia siguiente,
// y para saber que esa sentencia viene de un println hace falta el estado.
//
// Como fija el valor exacto, tambien BORRA las lineas de mas que trajera el fuente: en
// print-0 el main tiene cuatro y el golden ninguna.
//
// El salto que separa dos sentencias va siempre y las lineas en blanco se suman arriba,
// de ahi el "\n" mas los que pida la config.
class LineBreaksAfterPrintlnRule(private val blankLines: BlankLines? = null) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? {
        if (blankLines == null || state.lastStatementHead != TokenType.PRINTLN) return null
        if (!startsStatementAfterSemicolon(prev, current)) return null

        return "\n" + blankLines.render()
    }
}
