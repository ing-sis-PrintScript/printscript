package org.printscript.formatter.rules

import org.printscript.token.Token

// Decide el espacio que va ANTES de un token. Devolver null es "no me compete", y
// entonces el formatter deja el que traia el fuente. Mismo contrato que TokenRule.match
// en el lexer, que devuelve null cuando la regla no reconoce el caracter.
//
// No hace falta un parametro "next": una regla sobre el espacio DESPUES de un token se
// escribe como el espacio ANTES del que le sigue, mirando prev. Asi el formatter nunca
// necesita leer un token adelantado.
fun interface SpacingRule {
    fun spacingFor(
        prev: Token?,
        current: Token,
    ): String?
}
