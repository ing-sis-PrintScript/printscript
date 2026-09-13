package org.printscript.parser

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.statements.StatementParsers
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.token.TokenSource

class Parser(
    private val statements: StatementParsers,
    private val recovery: RecoveryStrategy,
) {
    fun parse(source: TokenSource): Sequence<Result<Statement, PrintScriptError>> = ParsedStatements(source, ::step)

    private fun step(stream: TokenStream): Parsed<Result<Statement, PrintScriptError>>? {
        if (stream.atEnd()) return null

        return when (val result = statements.parse(stream)) {
            is Result.Success -> Parsed(Result.Success(result.value.value), result.value.rest)
            is Result.Failure -> Parsed(result, recovery.recover(stream))
        }
    }
}

// Se recorre UNA sola vez, y esa es la razon de que exista en vez de un
// generateSequence: apenas alguien pide el iterador, la secuencia suelta el primer
// eslabon. Mientras lo sostenga sostiene tambien todos los siguientes --cada eslabon
// apunta al que sigue-- y un programa grande no entra en memoria.
//
// generateSequence no sirve para esto: guarda la lambda del seed adentro del objeto
// Sequence, y esa lambda captura la fuente mientras dure la iteracion.
private class ParsedStatements(
    source: TokenSource,
    private val step: (TokenStream) -> Parsed<Result<Statement, PrintScriptError>>?,
) : Sequence<Result<Statement, PrintScriptError>> {
    // var y nullable a proposito: ponerlo en null es lo que corta la referencia.
    // Sin val en el parametro del constructor, esta es la unica que queda.
    private var start: TokenSource? = source

    override fun iterator(): Iterator<Result<Statement, PrintScriptError>> {
        val first = requireNotNull(start) { "esta secuencia se recorre una sola vez" }
        start = null

        return object : AbstractIterator<Result<Statement, PrintScriptError>>() {
            // Donde hay que seguir parseando. Se calcula uno recien cuando lo piden:
            // eso es lo que hace que entregar un statement no lea los tokens del que
            // sigue. Y cada paso PISA al anterior, que es lo que deja sin apuntadores
            // al eslabon de la fuente ya consumido.
            private var pending: TokenStream? = TokenStream(first)

            override fun computeNext() {
                val from = pending
                val parsed = if (from == null) null else step(from)

                if (parsed == null) {
                    done()
                } else {
                    pending = parsed.rest
                    setNext(parsed.value)
                }
            }
        }
    }
}
