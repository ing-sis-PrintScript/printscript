package org.printscript.runner

import org.printscript.common.Version
import org.printscript.common.errorOrNull
import org.printscript.lexer.source.StringSourceReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// De punta a punta --lexer, parser e interpreter-- sobre lo que 1.1 agrega hasta ahora:
// const y el tipo boolean.
class PrintScript11RunnerTest {
    private val io = RecordingIO()

    private fun correr(
        fuente: String,
        version: Version = Version.V11,
    ) = ExecuteRunner(version, io).execute { StringSourceReader(fuente) }

    @Test
    fun `una constante se declara y se lee`() {
        val result = correr("""const nombre: string = "Joe";println(nombre);""")

        assertNull(result.errorOrNull())
        assertEquals(listOf("Joe"), io.output())
    }

    @Test
    fun `un booleano se declara y se imprime`() {
        val result = correr("let activo: boolean = true;println(activo);")

        assertNull(result.errorOrNull())
        assertEquals(listOf("true"), io.output())
    }

    @Test
    fun `false tambien`() {
        correr("let activo: boolean = false;println(activo);")

        assertEquals(listOf("false"), io.output())
    }

    @Test
    fun `una constante no se puede reasignar`() {
        val error = correr("""const nombre: string = "Joe";nombre = "Pepe";""").errorOrNull()

        assertNotNull(error)
        assertEquals("La constante 'nombre' no se puede reasignar.", error.message)
    }

    // Sin valor, una constante no se puede leer ni escribir nunca: el parser lo corta.
    @Test
    fun `una constante sin valor no se acepta`() {
        val error = correr("const limite: number;").errorOrNull()

        assertNotNull(error)
        assertEquals("Una constante tiene que declararse con un valor", error.message)
    }

    // Una variable con let si puede declararse sin valor y asignarse despues.
    @Test
    fun `una variable sin valor se puede asignar despues`() {
        val result = correr("let limite: number;limite = 10;println(limite);")

        assertNull(result.errorOrNull())
        assertEquals(listOf("10"), io.output())
    }

    @Test
    fun `una variable declarada con let si se reasigna`() {
        val result = correr("""let nombre: string = "Joe";nombre = "Pepe";println(nombre);""")

        assertNull(result.errorOrNull())
        assertEquals(listOf("Pepe"), io.output())
    }

    @Test
    fun `no se puede poner un numero en un boolean`() {
        val error = correr("let activo: boolean = 5;").errorOrNull()

        assertNotNull(error)
    }

    // Lo mismo corrido como 1.0 no llega ni a ejecutarse: 'const' y 'boolean' no son
    // keywords en esa version.
    @Test
    fun `lo de 1_1 falla si se corre como 1_0`() {
        assertNotNull(correr("""const nombre: string = "Joe";""", Version.V10).errorOrNull())
        assertNotNull(correr("let activo: boolean = true;", Version.V10).errorOrNull())
    }
}
