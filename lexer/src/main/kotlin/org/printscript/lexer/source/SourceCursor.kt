package org.printscript.lexer.source

import org.printscript.common.Position

internal class SourceCursor(lines: Sequence<String>) {
    private val iterator = lines.iterator()
    private var currentLine: String = ""
    private var lastEnd: Position = Position(1, 1)

    var lineNumber: Int = 0
        private set

    var index: Int = 0
        private set

    val line: String get() = currentLine

    fun moveToNextToken(): Boolean {
        while (true) {
            while (index < currentLine.length) {
                if (!currentLine[index].isWhitespace()) return true
                index++
            }

            lastEnd = Position(maxOf(lineNumber, 1), currentLine.length + 1)

            if (!iterator.hasNext()) return false

            currentLine = iterator.next()
            lineNumber++
            index = 0
        }
    }

    fun advanceTo(nextIndex: Int) {
        index = nextIndex
    }

    fun endPosition(): Position = lastEnd
}
