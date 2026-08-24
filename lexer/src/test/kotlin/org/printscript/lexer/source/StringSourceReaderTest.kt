package org.printscript.lexer.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class StringSourceReaderTest {
    private fun SourceReader.allLines(): List<String> =
        when (val result = nextLine()) {
            is LineReadResult.Success -> listOf(result.line) + result.remaining.allLines()
            LineReadResult.EndOfInput -> emptyList()
        }

    private fun successOf(result: LineReadResult): LineReadResult.Success {
        assertIs<LineReadResult.Success>(result, "esperaba una linea y vino EndOfInput")
        return result
    }

    private fun visible(text: String): String = text.replace("\n", "\\n").replace("\r", "\\r")

    @Test
    fun `una fuente vacia da una linea vacia y despues EndOfInput`() {
        val primera = successOf(StringSourceReader("").nextLine())

        assertEquals("", primera.line)
        assertEquals(LineReadResult.EndOfInput, primera.remaining.nextLine())
    }

    @Test
    fun `una sola linea sin salto final`() {
        assertEquals(listOf("let x;"), StringSourceReader("let x;").allLines())
    }

    @Test
    fun `varias lineas separadas por salto`() {
        assertEquals(listOf("uno", "dos", "tres"), StringSourceReader("uno\ndos\ntres").allLines())
    }

    @Test
    fun `un salto de linea final produce una linea vacia extra`() {
        assertEquals(listOf("a", ""), StringSourceReader("a\n").allLines())
    }

    @Test
    fun `las lineas vacias del medio se conservan`() {
        assertEquals(listOf("a", "", "b"), StringSourceReader("a\n\nb").allLines())
    }

    @Test
    fun `el retorno de carro con salto cuenta como un solo separador`() {
        assertEquals(listOf("a", "b"), StringSourceReader("a\r\nb").allLines())
    }

    @Test
    fun `el retorno de carro solo tambien separa lineas`() {
        assertEquals(listOf("a", "b"), StringSourceReader("a\rb").allLines())
    }

    @Test
    fun `leer dos veces del mismo reader devuelve exactamente lo mismo`() {
        val reader = StringSourceReader("uno\ndos")

        assertEquals(reader.nextLine(), reader.nextLine())
    }

    @Test
    fun `un remaining guardado a mitad de camino da siempre el mismo resto`() {
        val reader = StringSourceReader("uno\ndos\ntres")
        val despuesDeLaPrimera = successOf(reader.nextLine()).remaining

        val primerRecorrido = despuesDeLaPrimera.allLines()

        assertEquals(listOf("dos", "tres"), primerRecorrido)
        assertEquals(primerRecorrido, despuesDeLaPrimera.allLines())
        assertEquals(listOf("uno", "dos", "tres"), reader.allLines())
    }

    @Test
    fun `produce exactamente las mismas lineas que lineSequence`() {
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
                "a\rb\nc\r\nd",
                "let x: number = 5;\nprintln(x);\n",
                "   \n\t\n",
            )

        for (fuente in fuentes) {
            assertEquals(
                fuente.lineSequence().toList(),
                StringSourceReader(fuente).allLines(),
                "difiere para \"${visible(fuente)}\"",
            )
        }
    }

    @Test
    fun `un reader parado en el centinela ya no tiene lineas`() {
        val texto = "una\ndos"

        assertEquals(LineReadResult.EndOfInput, StringSourceReader(texto, texto.length + 1).nextLine())
    }

    @Test
    fun `parado en el largo del texto todavia queda la ultima linea`() {
        val texto = "una\ndos"

        val ultima = successOf(StringSourceReader(texto, texto.length).nextLine())

        assertEquals("", ultima.line, "desde length se emite la línea vacía final, no EndOfInput")
    }

    @Test
    fun `un offset fuera de rango no construye el reader`() {
        assertFailsWith<IllegalArgumentException> { StringSourceReader("abc", 5) }
        assertFailsWith<IllegalArgumentException> { StringSourceReader("abc", -1) }
    }
}
