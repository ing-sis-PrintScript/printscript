package org.printscript.formatter.engine

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.Formatter
import org.printscript.formatter.config.Indent
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
//
// La sangria NO es una regla, y esa es la unica cosa que el formatter decide por su
// cuenta. Una regla contesta cuantos saltos de linea van; la sangria contesta con que
// sigue el renglon despues del ultimo. No compiten: se componen. Si fuera una regla
// mas, prender indent-inside-if junto con line-breaks-after-println haria que una le
// pise las lineas en blanco a la otra.
class TokenFormatter(
    private val matcher: SpacingMatcher = SpacingMatcher(),
    private val indent: Indent? = null,
) : Formatter {
    override fun format(tokens: TokenSource): Sequence<Result<FormattedCode, PrintScriptError>> =
        FormattedTokens(tokens, matcher, indent)
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
    indent: Indent?,
    prev: Token?,
    token: Token,
    state: FormattingState,
): FormattedCode {
    val spacing = matcher.spacingFor(prev, token, state) ?: token.leadingTrivia.text
    return FormattedCode(indented(spacing, indent, token, state) + sourceTextOf(token))
}

// La sangria del renglon que este espacio abre (indent-inside-if).
//
// Se aplica sobre el espacio YA decidido, venga de una regla o del fuente, y por eso
// no compite con ninguna: reemplaza lo que haya despues del ultimo salto de linea y
// deja los saltos intactos.
//
// Un espacio sin salto de linea no abre ningun renglon, asi que no se toca. Eso es lo
// que deja quieto al "{" cuando va en la misma linea.
private fun indented(
    spacing: String,
    indent: Indent?,
    token: Token,
    state: FormattingState,
): String {
    if (indent == null || !spacing.contains('\n')) return spacing

    // El "}" cierra el bloque, asi que se escribe con la sangria de AFUERA. El estado
    // todavia no bajo de nivel: baja recien despues de escribir el token.
    val depth = if (token.type == TokenType.RBRACE) state.blockDepth - 1 else state.blockDepth

    return spacing.substringBeforeLast('\n') + "\n" + indent.render(depth.coerceAtLeast(0))
}

// Misma razon que ParsedStatements en el parser: mientras alguien sostenga el primer
// eslabon de la fuente, la cadena entera queda viva y el archivo no entra en memoria.
// Por eso la secuencia suelta la referencia apenas le piden el iterador.
private class FormattedTokens(
    source: TokenSource,
    private val matcher: SpacingMatcher,
    private val indent: Indent?,
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
                        val formatted = render(matcher, indent, previous, token, state)
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

            // Una llave abre o cierra bloque, y ademas termina la sentencia en curso:
            // lo que venga adentro arranca una nueva.
            private fun advanceStatement(token: Token) {
                when (token.type) {
                    TokenType.SEMICOLON -> {
                        state = state.copy(lastStatementHead = currentHead)
                        currentHead = null
                    }

                    TokenType.LBRACE -> {
                        state = state.copy(blockDepth = state.blockDepth + 1)
                        currentHead = null
                    }

                    TokenType.RBRACE -> {
                        state = state.copy(blockDepth = state.blockDepth - 1)
                        currentHead = null
                    }

                    else -> if (currentHead == null && token.type != TokenType.EOF) currentHead = token.type
                }
            }
        }
    }
}
