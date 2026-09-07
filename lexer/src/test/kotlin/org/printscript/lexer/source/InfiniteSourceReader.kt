package org.printscript.lexer.source

class LineReadCounter {
    var total: Int = 0
        private set

    fun countRead() {
        total++
    }
}

// A propósito NO cumple la persistencia que promete SourceReader: devuelve `this` y
// cuenta cada lectura, que es justamente lo que permite medir la pereza del lexer.
// No la tomes como implementación de referencia.
class InfiniteSourceReader(
    private val line: String,
    private val counter: LineReadCounter,
) : SourceReader {
    override fun nextLine(): LineReadResult {
        counter.countRead()
        return LineReadResult.Success(line, this)
    }
}
