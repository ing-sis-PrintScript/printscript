package org.printscript.parser

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource

class ResultTokenSource(
    private val results: List<Result<Token, PrintScriptError>>,
    private val offset: Int = 0,
) : TokenSource {
    override fun nextToken(): TokenReadResult {
        if (offset == results.size) return TokenReadResult.EndOfInput

        val rest = ResultTokenSource(results, offset + 1)
        return when (val result = results[offset]) {
            is Result.Success -> TokenReadResult.Success(result.value, rest)
            is Result.Failure -> TokenReadResult.Failure(result.error, rest)
        }
    }
}

class TokenReadCounter {
    var total: Int = 0
        private set

    fun countRead() {
        total++
    }
}

class CountingTokenSource(
    private val tokens: List<Token>,
    private val counter: TokenReadCounter,
    private val offset: Int = 0,
) : TokenSource {
    override fun nextToken(): TokenReadResult {
        if (offset == tokens.size) return TokenReadResult.EndOfInput

        counter.countRead()
        return TokenReadResult.Success(tokens[offset], CountingTokenSource(tokens, counter, offset + 1))
    }
}
