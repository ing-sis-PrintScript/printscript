package org.printscript.lexer.source

private const val LINE_FEED = '\n'
private const val CARRIAGE_RETURN = '\r'

data class StringSourceReader(
    private val text: String,
    private val offset: Int = 0,
) : SourceReader {
    private val endOfInputOffset: Int get() = text.length + 1

    init {
        require(offset in 0..endOfInputOffset) {
            "offset $offset fuera de rango para un texto de ${text.length} caracteres"
        }
    }

    override fun nextLine(): LineReadResult {
        if (offset == endOfInputOffset) return LineReadResult.EndOfInput

        val separator = separatorFrom(offset) ?: return lastLine()

        return LineReadResult.Success(
            text.substring(offset, separator.start),
            StringSourceReader(text, separator.end),
        )
    }

    private fun lastLine(): LineReadResult.Success =
        LineReadResult.Success(text.substring(offset), StringSourceReader(text, endOfInputOffset))

    private fun separatorFrom(from: Int): LineSeparator? =
        (from until text.length)
            .firstOrNull { text[it].isLineBreak() }
            ?.let { separatorAt(it) }

    private fun separatorAt(index: Int): LineSeparator =
        if (isCarriageReturnLineFeedAt(index)) {
            LineSeparator(index, index + 2)
        } else {
            LineSeparator(index, index + 1)
        }

    private fun isCarriageReturnLineFeedAt(index: Int): Boolean =
        text[index] == CARRIAGE_RETURN && index + 1 < text.length && text[index + 1] == LINE_FEED
}

private data class LineSeparator(val start: Int, val end: Int)

private fun Char.isLineBreak(): Boolean = this == LINE_FEED || this == CARRIAGE_RETURN
