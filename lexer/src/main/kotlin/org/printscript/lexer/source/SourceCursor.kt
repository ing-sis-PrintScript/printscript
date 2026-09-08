package org.printscript.lexer.source

import org.printscript.common.Position
import org.printscript.token.Trivia

internal data class SourceCursor(
    private val lines: SourceReader,
    val line: String,
    val lineNumber: Int,
    val index: Int,
    private val lastEnd: Position,
    // Lo que se saltea desde el token anterior. Antes se tiraba; ahora se junta
    // aca hasta que alguien la consume, y advanceTo la vacia.
    val trivia: Trivia = Trivia.EMPTY,
) {
    fun moveToNextToken(): ScanResult = scan(this)

    fun advanceTo(nextIndex: Int): SourceCursor = copy(index = nextIndex, trivia = Trivia.EMPTY)

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
        stoppingAt((index until line.length).firstOrNull { !line[it].isWhitespace() } ?: line.length)

    private fun stoppingAt(next: Int): SourceCursor =
        copy(index = next, trivia = triviaPlus(line.substring(index, next)))

    // Sin whitespace devolvemos la misma trivia: en un archivo de 32K lineas,
    // no alocar por cada token pegado al anterior se nota.
    private fun triviaPlus(more: String): Trivia = if (more.isEmpty()) trivia else Trivia(trivia.text + more)

    // El separador se lo come el reader, asi que el salto lo agregamos nosotros.
    // lineNumber 0 es el estado previo a la primera linea: ahi no hay salto que
    // agregar, o el primer token del archivo arrancaria con un "\n" inventado.
    private fun closingCurrentLine(): SourceCursor =
        copy(
            lastEnd = Position(maxOf(lineNumber, 1), line.length + 1),
            trivia = if (lineNumber >= 1) triviaPlus("\n") else trivia,
        )

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
