package org.printscript.lexer.source

import java.io.BufferedReader
import java.io.File
import java.io.InputStream

// Lee una fuente de texto linea por linea, sin cargarla entera en memoria.
//
// Cada instancia es un eslabon: sabe SU linea y como conseguir el siguiente
// eslabon. La linea se lee recien cuando alguien llama a nextLine(), y Lazy la
// guarda para que preguntar dos veces al mismo eslabon devuelva lo mismo
// --que es lo que el contrato de SourceReader promete--.
//
// En memoria hay, a lo sumo, el buffer del BufferedReader (8 KB) y la linea
// actual. Un archivo de 7 TB nunca entra entero.
//
// OJO: mientras alguien sostenga el PRIMER eslabon, toda la cadena queda viva y
// el ahorro desaparece. Por eso los comandos lo crean en linea, sin guardarlo en
// ninguna variable: asi cada eslabon queda sin referencias apenas se avanza.
class StreamSourceReader private constructor(
    private val read: Lazy<LineReadResult>,
) : SourceReader {
    override fun nextLine(): LineReadResult = read.value

    companion object {
        // El archivo lo abrimos nosotros, asi que al terminar lo cerramos.
        fun of(file: File): SourceReader = chainOver(file.bufferedReader(), closeAtEnd = true)

        // El stream nos llega abierto: cerrarlo es de quien lo abrio, no nuestro.
        fun of(stream: InputStream): SourceReader = chainOver(stream.bufferedReader(), closeAtEnd = false)

        // closeAtEnd viaja por toda la cadena porque el eslabon que llega al final
        // es otro objeto, creado mucho despues que la fabrica: tiene que heredar
        // la politica del anterior para saber si le toca cerrar.
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
