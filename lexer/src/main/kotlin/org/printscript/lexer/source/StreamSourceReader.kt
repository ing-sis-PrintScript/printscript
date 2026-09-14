package org.printscript.lexer.source

import java.io.BufferedReader
import java.io.File
import java.io.InputStream

class StreamSourceReader private constructor(
    private val read: Lazy<LineReadResult>,
) : SourceReader {
    override fun nextLine(): LineReadResult = read.value

    companion object {
        fun of(file: File): SourceReader = chainOver(file.bufferedReader(), closeAtEnd = true)

        fun of(stream: InputStream): SourceReader = chainOver(stream.bufferedReader(), closeAtEnd = false)

        private fun chainOver(
            reader: BufferedReader,
            closeAtEnd: Boolean,
        ): SourceReader = StreamSourceReader(lazy { readOne(reader, closeAtEnd) })

        private fun readOne(
            reader: BufferedReader,
            closeAtEnd: Boolean,
        ): LineReadResult {
            val line = reader.readLine()

            if (line == null) {
                if (closeAtEnd) reader.close()
                return LineReadResult.EndOfInput
            }

            return LineReadResult.Success(line, chainOver(reader, closeAtEnd))
        }
    }
}
