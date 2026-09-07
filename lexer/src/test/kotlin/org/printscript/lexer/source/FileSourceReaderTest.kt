package org.printscript.lexer.source

import org.printscript.lexer.Lexer
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import org.printscript.token.TokenType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FileSourceReaderTest {
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

        assertEquals(listOf("let x: number = 5;", "println(x);"), lineasDe(FileSourceReader.of(file)))
    }

    @Test
    fun `una linea en blanco tambien es una linea`() {
        val file = archivoCon("uno\n\ntres\n")

        assertEquals(listOf("uno", "", "tres"), lineasDe(FileSourceReader.of(file)))
    }

    @Test
    fun `un archivo vacio no tiene lineas`() {
        assertEquals(emptyList(), lineasDe(FileSourceReader.of(archivoCon(""))))
    }

    @Test
    fun `preguntarle dos veces al mismo reader da lo mismo`() {
        val reader = FileSourceReader.of(archivoCon("uno\ndos\n"))

        val primera = assertIs<LineReadResult.Success>(reader.nextLine())
        val segunda = assertIs<LineReadResult.Success>(reader.nextLine())

        assertEquals(primera.line, segunda.line)
    }

    /**
     * Compara los tokens y no los TokenReadResult: adentro del remaining viaja el
     * SourceReader concreto, y un StringSourceReader nunca va a ser igual a un
     * FileSourceReader. Lo que tiene que coincidir es lo que sale, no con que se hizo.
     */
    private fun tokensDe(source: TokenSource): List<Token> =
        drain(source).filterIsInstance<TokenReadResult.Success>().map { it.token }

    private fun tokensSinEof(source: TokenSource): List<Token> = tokensDe(source).filterNot { it.type == TokenType.EOF }

    @Test
    fun `el mismo programa desde un archivo o desde un String da los mismos tokens`() {
        val programa = "let name: string = \"Joe\";\nprintln(name);\n"

        val desdeArchivo = tokensSinEof(Lexer().tokenize(FileSourceReader.of(archivoCon(programa))))
        val desdeString = tokensSinEof(Lexer().tokenize(StringSourceReader(programa)))

        assertEquals(desdeString, desdeArchivo)
    }

    /**
     * PENDIENTE antes de publicar el release.
     *
     * Los dos readers no se ponen de acuerdo en cuantas lineas tiene un archivo
     * que termina en salto de linea:
     *
     *   StringSourceReader   "a\nb\n" son 3 lineas, la ultima vacia   (vista de split)
     *   FileSourceReader     "a\nb\n" son 2 lineas                    (convencion Unix, readLine)
     *
     * Los tokens reales son identicos; lo unico que cambia es donde cae el EOF, y
     * por lo tanto el mensaje de error cuando falta algo al final del archivo.
     *
     * Este test fija la diferencia por escrito para que no quede tapada. El dia
     * que se unifiquen los dos readers, va a fallar y avisar.
     */
    @Test
    fun `los dos readers difieren en donde cae el EOF si el archivo termina en salto de linea`() {
        val programa = "let x: number = 5;\n"

        val eofArchivo = tokensDe(Lexer().tokenize(FileSourceReader.of(archivoCon(programa)))).last()
        val eofString = tokensDe(Lexer().tokenize(StringSourceReader(programa))).last()

        assertEquals(TokenType.EOF, eofArchivo.type)
        assertEquals(TokenType.EOF, eofString.type)

        assertEquals(1, eofArchivo.range.start.line, "readLine termina en la ultima linea con texto")
        assertEquals(2, eofString.range.start.line, "el salto final abre una linea mas")
    }
}
