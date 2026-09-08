package org.printscript.formatter.rules

import org.printscript.formatter.config.Spacing
import org.printscript.token.Token
import org.printscript.token.TokenType

// El espacio a los dos lados de un tipo de token: el de adelante y el del que le sigue.
//
// Es la forma que comparten casi todas las reglas de espaciado del TCK --el ':' de una
// declaracion, el '=' y los operadores--, asi que va una sola vez y se parametriza con
// que tokens mira. Sumar otra es agregar una fabrica abajo, no una clase nueva.
//
// null en before o after es "esa clave no vino en el config": no se toca ese espacio.
// Una regla apagada devuelve null siempre, que es lo mismo que no estar.
class SurroundingSpacing(
    private val types: Set<TokenType>,
    private val before: Spacing? = null,
    private val after: Spacing? = null,
) : SpacingRule {
    override fun spacingFor(
        prev: Token?,
        current: Token,
        state: FormattingState,
    ): String? =
        when {
            current.type in types -> before?.render()
            prev?.type in types -> after?.render()
            else -> null
        }
}

private val OPERATORS = setOf(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH)

private val COLON = setOf(TokenType.COLON)

private val ASSIGN = setOf(TokenType.ASSIGN)

// enforce-spacing-before-colon-in-declaration / enforce-spacing-after-colon-in-declaration.
// En PrintScript el ':' solo aparece en una declaracion, asi que no hace falta preguntar
// en que sentencia estamos.
fun colonSpacing(
    before: Spacing?,
    after: Spacing?,
): SpacingRule = SurroundingSpacing(COLON, before, after)

// enforce-spacing-around-equals y enforce-no-spacing-around-equals: dos claves del TCK
// que gobiernan el mismo espacio, por eso llegan ya resueltas en un solo Spacing.
fun assignmentSpacing(around: Spacing?): SpacingRule = SurroundingSpacing(ASSIGN, around, around)

// mandatory-space-surrounding-operations. Es un interruptor: prendida significa un
// espacio de cada lado, apagada significa no opinar.
fun operatorSpacing(mandatory: Boolean): SpacingRule {
    val spacing = if (mandatory) Spacing.SINGLE else null
    return SurroundingSpacing(OPERATORS, spacing, spacing)
}
