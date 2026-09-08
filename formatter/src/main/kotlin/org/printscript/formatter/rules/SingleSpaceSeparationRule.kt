package org.printscript.formatter.rules

import org.printscript.token.Token
import org.printscript.token.TokenType

// Un unico espacio entre todos los tokens (mandatory-single-space-separation).
//
// Es la regla mas amplia: opina de cada espacio, no de uno en particular. Por eso va
// ULTIMA en la lista, para que cualquier regla mas especifica pueda ganarle.
//
// Dos cosas que salen del golden y no del nombre:
//   - el ';' no lleva espacio adelante ("... thing";  no  "... thing" ;)
//   - donde el fuente traia un salto de linea, el salto manda: los statements no se
//     juntan en un renglon. Devolver null es justamente "dejar lo que habia".
class SingleSpaceSeparationRule(private val mandatory: Boolean = false) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? {
        if (!mandatory || prev == null) return null
        if (current.type == TokenType.SEMICOLON || current.type == TokenType.EOF) return ""
        if (current.leadingTrivia.lineBreaks > 0) return null

        return " "
    }
}
