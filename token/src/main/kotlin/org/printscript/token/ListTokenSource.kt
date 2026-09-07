package org.printscript.token

data class ListTokenSource(
    private val tokens: List<Token>,
    private val offset: Int = 0,
) : TokenSource {
    override fun nextToken(): TokenReadResult =
        if (offset == tokens.size) {
            TokenReadResult.EndOfInput
        } else {
            TokenReadResult.Success(tokens[offset], ListTokenSource(tokens, offset + 1))
        }
}
