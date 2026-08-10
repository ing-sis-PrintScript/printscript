package org.printscript.lexer.rules

import org.printscript.common.Result
import org.printscript.lexer.LexicalError
import org.printscript.token.Token

/**
 * Una regla reconoce UN tipo de token, empezando exactamente en [from].
 *
 * Tres respuestas posibles:
 *   null     → "esto no es lo mío", que el matcher pruebe la siguiente regla
 *   Success  → reconocí un token
 *   Failure  → esto es mío pero está mal escrito (ej: un string que no cierra)
 *
 * Ese tercer caso es la razón de devolver Result y no Token directamente: si
 * StringRule devolviera null ante "hola sin cerrar, el error final sería
 * "caracter inesperado comilla", que no ayuda a nadie.
 */
interface TokenRule {
    fun match(line: String, from: Int, lineNumber: Int): Result<Token, LexicalError>?
}
