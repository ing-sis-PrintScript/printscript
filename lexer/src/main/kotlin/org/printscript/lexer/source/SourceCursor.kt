package org.printscript.lexer.source

import org.printscript.common.Position

internal data class SourceCursor(
    private val lines: SourceReader,
    val line: String,
    val lineNumber: Int,
    val index: Int,
    private val lastEnd: Position,
) {
    fun moveToNextToken(): ScanResult = scan(this)

    fun advanceTo(nextIndex: Int): SourceCursor = copy(index = nextIndex)

    private tailrec fun scan(cursor: SourceCursor): ScanResult {
        val atToken = cursor.skippingSpaces()
        if (atToken.index < atToken.line.length) return ScanResult.Found(atToken)

        val closed = atToken.closingCurrentLine()
        return when (val read = closed.lines.nextLine()) {
            is LineReadResult.Success -> scan(closed.startingLine(read.line, read.remaining))
            LineReadResult.EndOfInput -> ScanResult.Exhausted(closed.lastEnd)
        }
    }

    private fun skippingSpaces(): SourceCursor =
        copy(index = (index until line.length).firstOrNull { !line[it].isWhitespace() } ?: line.length)

    private fun closingCurrentLine(): SourceCursor = copy(lastEnd = Position(maxOf(lineNumber, 1), line.length + 1))

    private fun startingLine(
        next: String,
        rest: SourceReader,
    ): SourceCursor = copy(lines = rest, line = next, lineNumber = lineNumber + 1, index = 0)

    companion object {
        fun from(lines: SourceReader): SourceCursor = SourceCursor(lines, "", 0, 0, Position(1, 1))
    }
}

internal sealed interface ScanResult {
    data class Found(val cursor: SourceCursor) : ScanResult

    data class Exhausted(val endPosition: Position) : ScanResult
}
