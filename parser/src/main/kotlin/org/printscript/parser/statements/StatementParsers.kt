package org.printscript.parser.statements

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.SyntaxError
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.describe

// El conjunto de parsers de statements de una version, y como se elige el que
// le corresponde a cada token.
//
// Es un fun interface --y no una List<StatementParser> pelada-- porque el
// IfParser va a estar ADENTRO de la lista que el mismo necesita para parsear el
// cuerpo del if: pedirla en el constructor seria pedirse a si mismo. Recibiendo
// "con que conseguir la lista" en vez de la lista, el ciclo se rompe. Es el
// mismo motivo por el que el lexer recibe un SourceFactory y no un SourceReader
// ya abierto.
fun interface StatementParsers {
    fun all(): List<StatementParser>

    // Un solo lugar decide que parser agarra cada token. Antes esto vivia
    // adentro de Parser; ahora lo usan Parser (los statements de arriba de todo)
    // y el BlockParser (los de adentro de las llaves), que hacen la misma
    // pregunta.
    fun parse(stream: TokenStream): Result<Parsed<Statement>, PrintScriptError> {
        val peeked = stream.peek()
        if (peeked is Result.Failure) return peeked

        val token = (peeked as Result.Success).value
        val parser =
            all().firstOrNull { it.canHandle(token.type) }
                ?: return Result.Failure(
                    SyntaxError("No se esperaba ${token.type.describe()} acá", token.range),
                )

        return parser.parse(stream)
    }
}
