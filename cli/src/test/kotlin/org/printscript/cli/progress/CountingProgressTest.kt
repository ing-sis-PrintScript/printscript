package org.printscript.cli.progress

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class CountingProgressTest {
    private val buffer = ByteArrayOutputStream()
    private val progress = CountingProgress(PrintStream(buffer))

    @Test
    fun `el ultimo renglon dice cuantas sentencias se parsearon`() {
        repeat(3) { progress.parsed() }

        assertEquals("3 sentencias", buffer.toString().substringAfterLast("Parseando... "))
    }

    @Test
    fun `done deja el renglon vacio para que el comando escriba su resultado`() {
        progress.parsed()
        progress.done()

        assertEquals("", buffer.toString().substringAfterLast("\r").trim())
    }
}
