package org.printscript.lexer

import org.printscript.token.Token

/**
 * El resultado de reconocer una unidad lexica: que token es y desde que indice
 * de la linea sigue el recorrido.
 *
 * Son dos hechos distintos y el Token solo puede representar uno:
 *   token.range → DONDE ESTA el token, para los errores y el linter
 *   nextIndex   → DONDE SIGO LEYENDO, que solo le importa al lexer
 *
 * Hoy los dos numeros coinciden, pero porque ninguna regla consume mas de lo
 * que emite. Una regla de comentarios se comeria "// bla" sin producir ningun
 * token, y ahi no habria range del cual deducir el avance.
 */
data class TokenMatch(val token: Token, val nextIndex: Int)
