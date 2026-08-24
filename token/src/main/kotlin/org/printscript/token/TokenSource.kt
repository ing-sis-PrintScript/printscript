package org.printscript.token

import org.printscript.common.PrintScriptError

interface TokenSource {
    fun nextToken(): TokenReadResult
}

sealed interface TokenReadResult {
    data class Success(val token: Token, val remaining: TokenSource) : TokenReadResult

    data class Failure(val error: PrintScriptError, val remaining: TokenSource) : TokenReadResult

    data object EndOfInput : TokenReadResult
}
