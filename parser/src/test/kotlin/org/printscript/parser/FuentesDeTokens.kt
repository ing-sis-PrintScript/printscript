package org.printscript.parser

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import java.util.concurrent.atomic.AtomicInteger

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

class ContadorDeTokens {
    private val leidos = AtomicInteger(0)

    fun registrarLectura() {
        leidos.incrementAndGet()
    }

    fun total(): Int = leidos.get()
}

class FuentePerezosa(
    private val tokens: List<Token>,
    private val contador: ContadorDeTokens,
    private val offset: Int = 0,
) : TokenSource {
    override fun nextToken(): TokenReadResult {
        if (offset == tokens.size) return TokenReadResult.EndOfInput

        contador.registrarLectura()
        return TokenReadResult.Success(tokens[offset], FuentePerezosa(tokens, contador, offset + 1))
    }
}
