package org.printscript.token

@ConsistentCopyVisibility
data class ListTokenSource private constructor(
    private val tokens: List<Token>,
    private val offset: Int,
) : TokenSource {
    constructor(tokens: List<Token>) : this(tokens.toList(), 0)

    init {
        require(offset in 0..tokens.size) {
            "offset $offset fuera de rango para ${tokens.size} tokens"
        }
    }

    override fun nextToken(): TokenReadResult =
        if (offset == tokens.size) {
            TokenReadResult.EndOfInput
        } else {
            TokenReadResult.Success(tokens[offset], ListTokenSource(tokens, offset + 1))
        }
}
