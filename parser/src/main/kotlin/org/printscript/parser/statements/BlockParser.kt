package org.printscript.parser.statements

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.expect
import org.printscript.parser.token.peekIs
import org.printscript.parser.token.skip
import org.printscript.token.Token
import org.printscript.token.TokenType

// Parsea "{ statement* }".
//
// Vive aparte del IfParser porque parsear un bloque no es parsear un if: el
// else usa exactamente el mismo bloque, y un while el dia de mañana tambien.
class BlockParser(private val statements: StatementParsers) {
    // Se guarda el "}" ademas de los statements. El token hay que consumirlo y
    // verificar que este igual, asi que tenerlo no cuesta nada, y es lo unico
    // con lo que se puede cerrar el Range del statement que contiene al bloque.
    data class Block(val statements: List<Statement>, val close: Token)

    fun parse(stream: TokenStream): Result<Parsed<Block>, PrintScriptError> {
        val openResult = stream.skip(TokenType.LBRACE, "para abrir el bloque")
        if (openResult is Result.Failure) return openResult

        val bodyResult = collect(Parsed(emptyList(), (openResult as Result.Success).value))
        if (bodyResult is Result.Failure) return bodyResult
        val (body, beforeClose) = (bodyResult as Result.Success).value

        val closeResult = beforeClose.expect(TokenType.RBRACE, "para cerrar el bloque")
        if (closeResult is Result.Failure) return closeResult

        val (close, afterClose) = (closeResult as Result.Success).value
        return Result.Success(Parsed(Block(body, close), afterClose))
    }

    // Los statements del bloque se juntan en una List --y no salen como Sequence
    // como los de arriba de todo-- porque corren solo si la condicion da true:
    // no se pueden ir entregando a medida que se leen.
    //
    // tailrec, igual que parseAdditions: Kotlin lo compila como un ciclo, asi
    // que un bloque largo no llena el stack y no hace falta nada mutable.
    private tailrec fun collect(parsed: Parsed<List<Statement>>): Result<Parsed<List<Statement>>, PrintScriptError> {
        // Si la fuente se termina antes del "}", peekIs da false, se sigue de
        // largo, y statements.parse falla con el fin de archivo. No cuelga.
        if (parsed.rest.peekIs(TokenType.RBRACE)) return Result.Success(parsed)

        return when (val next = statements.parse(parsed.rest)) {
            is Result.Failure -> next
            is Result.Success -> collect(Parsed(parsed.value + next.value.value, next.value.rest))
        }
    }
}
