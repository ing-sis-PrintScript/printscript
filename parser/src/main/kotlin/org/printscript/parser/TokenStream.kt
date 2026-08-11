package org.printscript.parser


import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.token.Token
import org.printscript.token.TokenType

/**
 * Lectura de tokens con lookahead de uno.
 *
 * Un Sequence solo deja pedir "dame el siguiente": no hay forma de mirar sin
 * consumir ni de volver atrás. Pero el parser necesita ver el próximo token para
 * decidir qué regla aplicar, SIN gastarlo, porque el que después construye el
 * nodo va a querer consumirlo él mismo.
 *
 * La solución es una "sala de espera" de un lugar: peek() saca el token del
 * Sequence pero lo deja guardado ahí, así el próximo next() lo encuentra. Desde
 * afuera parece que el token nunca se movió.
 */


class TokenStream(val tokens: Sequence<Result<Token, PrintScriptError>>) {

    private val iterator = tokens.iterator()
    private var buffered: Result<Token, PrintScriptError>? = null

    fun peek(): Result<Token, PrintScriptError> {
        val cached = buffered
        if (cached != null) return cached

        val next = readNext()
        buffered = next
        return next
    }

    fun next(): Result<Token, PrintScriptError> {
        val cached = buffered
        if (cached != null) {
            buffered = null
            return cached
        }
        return readNext()
    }

    /**
     * Consume el próximo token verificando que sea del tipo esperado.
     * Si no lo es, devuelve un SyntaxError apuntando al token que apareció.
     */
    fun expect(type: TokenType, what: String): Result<Token, PrintScriptError> {
        val result = next()
        return when (result) {
            is Result.Failure -> result
            is Result.Success -> {
                val token = result.value
                if (token.type == type) result
                else Result.Failure(SyntaxError("Se esperaba $what", token.range))
            }
        }
    }

    /** Consume tokens como el expect pero para datos que no aportan nada (":", ";", "="). */
    fun skip(type: TokenType, what: String): Result<Unit, PrintScriptError> =
        when (val result = expect(type, what)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> result
        }

    /** Chequea que el siguiente es de ese tipo. No consume */
    fun peekIs(type: TokenType): Boolean =
        when (val result = peek()) {
            is Result.Success -> result.value.type == type
            is Result.Failure -> false
        }

    fun atEnd(): Boolean = peekIs(TokenType.EOF)

    /**
     * Despues de un error, descarta tokens hasta llegar a un ";"
     *
     * Si no usamos esto, el parser sigue con un statement roto, y tiraria errores que son consecuencia
     * del primero.
     *
     * Esto se puede cambiar chequeando despues de un error si es un statement ("LET", "PRINTLN"),
     * chequear antes de codearlo.
     */
    fun synchronize() {
        while (!atEnd()) {
            val result = next()
            if (result is Result.Success && result.value.type == TokenType.SEMICOLON) return
        }
    }


    private fun readNext(): Result<Token, PrintScriptError> =
        if (iterator.hasNext()) iterator.next()
        else Result.Failure(SyntaxError("Fin de archivo inesperado", lastRange))

    private var lastRange = org.printscript.common.Range(
        org.printscript.common.Position(1, 1),
        org.printscript.common.Position(1, 1),
    )


}