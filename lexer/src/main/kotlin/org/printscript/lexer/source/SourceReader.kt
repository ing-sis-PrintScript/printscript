package org.printscript.lexer.source

interface SourceReader {
    fun nextLine(): LineReadResult
}

sealed interface LineReadResult {
    data class Success(val line: String, val remaining: SourceReader) : LineReadResult

    data object EndOfInput : LineReadResult
}
