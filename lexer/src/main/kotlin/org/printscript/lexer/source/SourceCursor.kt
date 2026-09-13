package org.printscript.lexer.source

import org.printscript.common.Position
import org.printscript.common.Range

internal data class SourceCursor(
    private val lines: SourceReader,
    val line: String,
    val lineNumber: Int,
    val index: Int,
    private val lastEnd: Position,
    // El espacio que se salteo desde el token anterior, y donde empieza. Antes se
    // tiraba; ahora se junta aca hasta que LexingTokenSource lo emite como un token
    // WHITESPACE, y clearSkipped lo vacia.
    val skipped: String = "",
    private val skippedStart: Position = Position(1, 1),
) {
    // De donde a donde va el espacio. Termina donde arranca el token que sigue: es
    // el unico final que se puede nombrar sin mirar el token, y el espacio no tiene
    // otra cosa que lo delimite.
    val skippedRange: Range get() = Range(skippedStart, Position(maxOf(lineNumber, 1), maxOf(index, 1)))

    fun moveToNextToken(): ScanResult = scan(this)

    fun advanceTo(nextIndex: Int): SourceCursor = copy(index = nextIndex, skipped = "")

    fun clearSkipped(): SourceCursor = copy(skipped = "")

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
        plusSkipped(line.substring(index, next), Position(maxOf(lineNumber, 1), index + 1)).copy(index = next)

    // Sin espacio devolvemos el mismo cursor: en un archivo de 32K lineas, no alocar
    // por cada token pegado al anterior se nota.
    private fun plusSkipped(
        more: String,
        at: Position,
    ): SourceCursor =
        when {
            more.isEmpty() -> this
            skipped.isEmpty() -> copy(skipped = more, skippedStart = at)
            else -> copy(skipped = skipped + more)
        }

    // El separador se lo come el reader, asi que el salto lo agregamos nosotros.
    // lineNumber 0 es el estado previo a la primera linea: ahi no hay salto que
    // agregar, o el primer token del archivo arrancaria con un "\n" inventado.
    private fun closingCurrentLine(): SourceCursor {
        val end = Position(maxOf(lineNumber, 1), line.length + 1)
        val closed = copy(lastEnd = end)
        return if (lineNumber >= 1) closed.plusSkipped("\n", end) else closed
    }

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
