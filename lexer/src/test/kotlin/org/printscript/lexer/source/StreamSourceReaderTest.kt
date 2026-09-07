package org.printscript.lexer.source

import org.printscript.lexer.Lexer
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import org.printscript.token.TokenType
import java.io.File
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class StreamSourceReaderTest {
    private fun archivoCon(texto: String): File {
        val file = File.createTempFile("printscript", ".ps")
        file.deleteOnExit()
        file.writeText(texto)
        return file
    }

    private fun lineasDe(reader: SourceReader): List<String> =
        when (val read = reader.nextLine()) {
            is LineReadResult.Success -> listOf(read.line) + lineasDe(read.remaining)
            LineReadResult.EndOfInput -> emptyList()
        }

    private fun drain(source: TokenSource): List<TokenReadResult> =
        when (val result = source.nextToken()) {
            is TokenReadResult.Success -> listOf(result) + drain(result.remaining)
            is TokenReadResult.Failure -> listOf(result)
            TokenReadResult.EndOfInput -> emptyList()
        }

    @Test
    fun `lee las lineas en orden`() {
        val file = archivoCon("let x: number = 5;\nprintln(x);\n")

        assertEquals(listOf("let x: number = 5;", "println(x);"), lineasDe(StreamSourceReader.of(file)))
    }

    @Test
    fun `una linea en blanco tambien es una linea`() {
        val file = archivoCon("uno\n\ntres\n")

        assertEquals(listOf("uno", "", "tres"), lineasDe(StreamSourceReader.of(file)))
    }

    @Test
    fun `un archivo vacio no tiene lineas`() {
        assertEquals(emptyList(), lineasDe(StreamSourceReader.of(archivoCon(""))))
    }

    @Test
    fun `preguntarle dos veces al mismo reader da lo mismo`() {
        val reader = StreamSourceReader.of(archivoCon("uno\ndos\n"))

        val primera = assertIs<LineReadResult.Success>(reader.nextLine())
        val segunda = assertIs<LineReadResult.Success>(reader.nextLine())

        assertEquals(primera.line, segunda.line)
    }

    /**
     * Compara los tokens y no los TokenReadResult: adentro del remaining viaja el
     * SourceReader concreto, y un StringSourceReader nunca va a ser igual a un
     * StreamSourceReader. Lo que tiene que coincidir es lo que sale, no con que se hizo.
     */
    private fun tokensDe(source: TokenSource): List<Token> =
        drain(source).filterIsInstance<TokenReadResult.Success>().map { it.token }

    private fun tokensSinEof(source: TokenSource): List<Token> = tokensDe(source).filterNot { it.type == TokenType.EOF }

    @Test
    fun `el mismo programa desde un archivo o desde un String da los mismos tokens`() {
        val programa = "let name: string = \"Joe\";\nprintln(name);\n"

        val desdeArchivo = tokensSinEof(Lexer().tokenize(StreamSourceReader.of(archivoCon(programa))))
        val desdeString = tokensSinEof(Lexer().tokenize(StringSourceReader(programa)))

        assertEquals(desdeString, desdeArchivo)
    }

    // Los dos readers tienen que ver el mismo archivo igual. Antes no era asi:
    // StringSourceReader seguia la convencion de lineSequence ("a\n" son dos lineas,
    // la ultima vacia) y StreamSourceReader la de readLine ("a\n" es una). Eso movia el
    // EOF de lugar y con el la posicion que sale en los mensajes de error.
    @Test
    fun `los dos readers ven las mismas lineas`() {
        val fuentes =
            listOf(
                "",
                "a",
                "a\n",
                "\n",
                "a\n\nb",
                "\n\n\n",
                "a\r\nb",
                "a\rb",
                "a\r\n\r\nb",
                "let x: number = 5;\nprintln(x);\n",
            )

        for (fuente in fuentes) {
            assertEquals(
                lineasDe(StringSourceReader(fuente)),
                lineasDe(StreamSourceReader.of(archivoCon(fuente))),
                "difieren para \"${fuente.replace("\n", "\\n").replace("\r", "\\r")}\"",
            )
        }
    }

    @Test
    fun `el EOF cae en el mismo lugar aunque el archivo termine en salto de linea`() {
        val programa = "let x: number = 5;\n"

        val eofArchivo = tokensDe(Lexer().tokenize(StreamSourceReader.of(archivoCon(programa)))).last()
        val eofString = tokensDe(Lexer().tokenize(StringSourceReader(programa))).last()

        assertEquals(TokenType.EOF, eofArchivo.type)
        assertEquals(eofString.range, eofArchivo.range)
    }

    // Espia para ver si nos pasamos de la raya y cerramos un stream que no abrimos.
    // El close() no delega a proposito: lo unico que nos importa es si lo llamaron.
    private class ClosingSpy(texto: String) : InputStream() {
        private val bytes = texto.byteInputStream()

        var closed = false
            private set

        override fun read(): Int = bytes.read()

        override fun close() {
            closed = true
        }
    }

    @Test
    fun `lee las mismas lineas desde un stream que desde un archivo`() {
        val programa = "let x: number = 5;\nprintln(x);\n"

        assertEquals(
            lineasDe(StreamSourceReader.of(archivoCon(programa))),
            lineasDe(StreamSourceReader.of(programa.byteInputStream())),
        )
    }

    // El TCK nos pasa un InputStream que abrio el, y lo puede seguir usando despues.
    // Cerrarlo seria cortarle el recurso a quien es su dueño.
    @Test
    fun `el stream que nos dan no se cierra al llegar al final`() {
        val spy = ClosingSpy("uno\ndos\n")

        lineasDe(StreamSourceReader.of(spy))

        assertFalse(spy.closed)
    }

    @Test
    fun `un stream vacio no tiene lineas`() {
        assertEquals(emptyList(), lineasDe(StreamSourceReader.of("".byteInputStream())))
    }
}
