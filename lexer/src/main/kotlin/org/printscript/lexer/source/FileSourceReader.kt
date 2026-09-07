package org.printscript.lexer.source

import java.io.BufferedReader
import java.io.File

/**
 * Lee un archivo linea por linea, sin cargarlo entero en memoria.
 *
 * Cada instancia es un eslabon: sabe SU linea y como conseguir el siguiente
 * eslabon. La linea se lee del disco recien cuando alguien llama a nextLine(),
 * y Lazy la guarda para que preguntar dos veces al mismo eslabon de lo mismo
 * --que es lo que el contrato de SourceReader promete--.
 *
 * En memoria hay, a lo sumo, el buffer del BufferedReader (8 KB) y la linea
 * actual. Un archivo de 7 TB nunca entra entero.
 *
 * OJO: mientras alguien sostenga el PRIMER eslabon, toda la cadena queda viva y
 * el ahorro desaparece. Por eso los comandos lo crean en linea, sin guardarlo en
 * ninguna variable: asi cada eslabon queda sin referencias apenas se avanza.
 */
class FileSourceReader private constructor(
    private val read: Lazy<LineReadResult>,
) : SourceReader {
    override fun nextLine(): LineReadResult = read.value

    companion object {
        fun of(file: File): SourceReader = chainOver(file.bufferedReader())

        private fun chainOver(reader: BufferedReader): SourceReader = FileSourceReader(lazy { readOne(reader) })

        private fun readOne(reader: BufferedReader): LineReadResult {
            val line = reader.readLine()

            if (line == null) {
                reader.close()
                return LineReadResult.EndOfInput
            }

            return LineReadResult.Success(line, chainOver(reader))
        }
    }
}
