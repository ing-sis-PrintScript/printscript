package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.lexer.TokenMatch

/**
 * Una regla reconoce UN tipo de token, empezando exactamente en [from].
 *
 * Tres respuestas posibles:
 *   null     → "esto no es lo mio", que el matcher pruebe la siguiente regla
 *   Success  → reconoci un token, y digo desde donde sigue el fuente
 *   Failure  → esto es mio pero esta mal escrito (ej: un string que no cierra)
 *
 * Ese tercer caso es la razon de devolver Result y no TokenMatch directamente:
 * si StringRule devolviera null ante "hola sin cerrar, el error final seria
 * "caracter inesperado comilla", que no ayuda a nadie.
 *
 * Que la regla devuelva el nextIndex es lo que le permite al Lexer no saber
 * nada del lenguaje: no tiene que deducir cuanto avanzar, se lo dicen.
 */
interface TokenRule {
    fun match(line: String, from: Int, lineNumber: Int): Result<TokenMatch, LexicalError>?
}
