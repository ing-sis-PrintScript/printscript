package org.printscript.cli.progress

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CountingProgressTest {
    private val buffer = ByteArrayOutputStream()
    private val progress = CountingProgress(PrintStream(buffer))

    @Test
    fun `el ultimo renglon dice cuantas sentencias se parsearon`() {
        repeat(3) { progress.parsed() }

        assertEquals("3 sentencias", buffer.toString().substringAfterLast("Parseando... "))
    }

    // El contador se reescribe sobre si mismo con \r: un solo renglon, no uno por
    // sentencia.
    @Test
    fun `el contador se dibuja siempre en el mismo renglon`() {
        repeat(3) { progress.parsed() }

        assertEquals(3, buffer.toString().count { it == '\r' })
        assertTrue(!buffer.toString().contains('\n'), "no tiene que bajar de renglon mientras cuenta")
    }

    // Cierra el renglon en vez de borrarlo: el total queda a la vista y lo que el
    // comando escriba despues arranca abajo.
    @Test
    fun `done cierra el renglon y deja el total a la vista`() {
        repeat(2) { progress.parsed() }
        progress.done()

        val salida = buffer.toString()

        assertTrue(salida.contains("Parseando... 2 sentencias"), salida)
        assertTrue(salida.endsWith("\n"), "tiene que cerrar el renglon: $salida")
    }

    // Sin sentencias no se dibujo nada, asi que tampoco hay renglon que cerrar.
    @Test
    fun `done no escribe nada si no se parseo ninguna sentencia`() {
        progress.done()

        assertEquals("", buffer.toString())
    }
}
