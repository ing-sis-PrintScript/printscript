package org.printscript.lexer.source

import org.printscript.lexer.Lexer
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
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

    @Test
    fun `el mismo programa desde un archivo o desde un String da los mismos tokens`() {
        val programa = "let name: string = \"Joe\";\nprintln(name);\n"

        val desdeArchivo = drain(Lexer().tokenize(FileSourceReader.of(archivoCon(programa))))
        val desdeString = drain(Lexer().tokenize(StringSourceReader(programa)))

        assertEquals(desdeString, desdeArchivo)
    }
}
