package org.printscript.formatter.engine

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.Formatter
import org.printscript.formatter.rules.FormattingState
import org.printscript.formatter.rules.SpacingMatcher
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import org.printscript.token.TokenType

// Formatea recorriendo los TOKENS, no el AST.
//
// Cada token se escribe como la trivia que traia adelante mas su texto. Sin reglas
// activas eso devuelve el archivo tal cual, que es lo que piden los goldens del TCK:
// el formatter preserva y solo reescribe donde una regla le compete.
//
// Un elemento de la secuencia es UN token, no una sentencia. El tipo no lo dice, asi
// que queda escrito aca.
//
// Sin reglas el matcher no contesta nunca, y entonces la salida es el fuente tal cual.
class TokenFormatter(private val matcher: SpacingMatcher = SpacingMatcher()) : Formatter {
    override fun format(tokens: TokenSource): Sequence<Result<FormattedCode, PrintScriptError>> =
        FormattedTokens(tokens, matcher)
}

// El lexer guarda el string sin comillas --son delimitadores, no contenido--, asi que
// al escribirlo hay que volver a ponerlas. Siempre dobles: el token no sabe cual traia
// el fuente. Limitacion conocida: un fuente con 'a' sale con "a". Ninguno de los 120
// .ps del TCK usa comilla simple.
private fun sourceTextOf(token: Token): String =
    when (token.type) {
        TokenType.STRING_LITERAL -> "\"${token.value}\""
        else -> token.value
    }

// El corazon del modelo incremental: si ninguna regla opina, va el espacio original.
private fun render(
    matcher: SpacingMatcher,
    prev: Token?,
    token: Token,
    state: FormattingState,
): FormattedCode =
    FormattedCode((matcher.spacingFor(prev, token, state) ?: token.leadingTrivia.text) + sourceTextOf(token))

// Misma razon que ParsedStatements en el parser: mientras alguien sostenga el primer
// eslabon de la fuente, la cadena entera queda viva y el archivo no entra en memoria.
// Por eso la secuencia suelta la referencia apenas le piden el iterador.
private class FormattedTokens(
    source: TokenSource,
    private val matcher: SpacingMatcher,
) : Sequence<Result<FormattedCode, PrintScriptError>> {
    // var y nullable a proposito: ponerlo en null es lo que corta la referencia.
    private var start: TokenSource? = source

    override fun iterator(): Iterator<Result<FormattedCode, PrintScriptError>> {
        val first = requireNotNull(start) { "esta secuencia se recorre una sola vez" }
        start = null

        return object : AbstractIterator<Result<FormattedCode, PrintScriptError>>() {
            // Cada paso PISA al anterior, que es lo que deja sin apuntadores al eslabon
            // ya consumido.
            private var pending: TokenSource? = first

            // El token anterior alcanza para casi todas las reglas.
            private var previous: Token? = null

            // Lo que no entra en dos tokens. currentHead es con que arranco la sentencia
            // que se esta recorriendo; al cerrarla con ';' pasa a ser la "anterior".
            private var currentHead: TokenType? = null
            private var state = FormattingState()

            override fun computeNext() {
                when (val read = pending?.nextToken()) {
                    is TokenReadResult.Success -> {
                        val token = read.token
                        // Se formatea con el estado que dejo la sentencia anterior, y
                        // recien despues se actualiza: si no, un ';' se veria a si mismo.
                        val formatted = render(matcher, previous, token, state)
                        pending = read.remaining
                        previous = token
                        advanceStatement(token)
                        setNext(Result.Success(formatted))
                    }

                    // Un error lexico corta: lo que sigue ya no es confiable.
                    is TokenReadResult.Failure -> {
                        pending = null
                        setNext(Result.Failure(read.error))
                    }

                    TokenReadResult.EndOfInput, null -> done()
                }
            }

            private fun advanceStatement(token: Token) {
                if (token.type == TokenType.SEMICOLON) {
                    state = FormattingState(lastStatementHead = currentHead)
                    currentHead = null
                } else if (currentHead == null && token.type != TokenType.EOF) {
                    currentHead = token.type
                }
            }
        }
    }
}
