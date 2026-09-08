package org.printscript.formatter.rules

import org.printscript.formatter.config.Spacing
import org.printscript.token.Token
import org.printscript.token.TokenType

// Las dos reglas del ':' de una declaracion, que el TCK manda como
// enforce-spacing-before-colon-in-declaration y enforce-spacing-after-colon-in-declaration.
//
// null en un parametro es "esa clave no vino en el config": no se toca ese espacio.
// En PrintScript el ':' solo aparece en una declaracion, asi que no hay que preguntar
// en que sentencia estamos.
class ColonSpacingRule(
    private val before: Spacing? = null,
    private val after: Spacing? = null,
) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
    ): String? =
        when {
            current.type == TokenType.COLON -> before?.render()
            prev?.type == TokenType.COLON -> after?.render()
            else -> null
        }
}
