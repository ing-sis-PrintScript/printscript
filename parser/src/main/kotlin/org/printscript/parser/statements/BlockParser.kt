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

class BlockParser(private val statements: StatementParsers) {
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

    private tailrec fun collect(parsed: Parsed<List<Statement>>): Result<Parsed<List<Statement>>, PrintScriptError> {
        if (parsed.rest.peekIs(TokenType.RBRACE)) return Result.Success(parsed)

        return when (val next = statements.parse(parsed.rest)) {
            is Result.Failure -> next
            is Result.Success -> collect(Parsed(parsed.value + next.value.value, next.value.rest))
        }
    }
}
