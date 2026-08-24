package org.printscript.token

data class ListTokenSource(
    private val tokens: List<Token>,
    private val offset: Int = 0,
) : TokenSource {
    private val snapshot: List<Token> = tokens.toList()

    init {
        require(offset in 0..snapshot.size) {
            "offset $offset fuera de rango para ${snapshot.size} tokens"
        }
    }

    override fun nextToken(): TokenReadResult =
        if (offset == snapshot.size) {
            TokenReadResult.EndOfInput
        } else {
            TokenReadResult.Success(snapshot[offset], ListTokenSource(snapshot, offset + 1))
        }
}
