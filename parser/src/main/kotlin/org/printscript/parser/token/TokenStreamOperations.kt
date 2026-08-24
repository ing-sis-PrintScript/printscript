package org.printscript.parser.token

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map
import org.printscript.parser.SyntaxError
import org.printscript.token.Token
import org.printscript.token.TokenType

fun TokenStream.next(): Result<Parsed<Token>, PrintScriptError> = peek().map { Parsed(it, advance()) }

fun TokenStream.expect(
    type: TokenType,
    context: String? = null,
): Result<Parsed<Token>, PrintScriptError> =
    peek().flatMap { token ->
        if (token.type == type) {
            Result.Success(Parsed(token, advance()))
        } else {
            Result.Failure(SyntaxError(expectedMessage(type, context), token.range))
        }
    }

fun TokenStream.skip(
    type: TokenType,
    context: String? = null,
): Result<TokenStream, PrintScriptError> = expect(type, context).map { it.rest }

private fun expectedMessage(
    type: TokenType,
    context: String?,
): String = if (context == null) "Se esperaba ${type.describe()}" else "Se esperaba ${type.describe()} $context"

fun TokenStream.peekIs(type: TokenType): Boolean =
    when (val result = peek()) {
        is Result.Success -> result.value.type == type
        is Result.Failure -> false
    }
