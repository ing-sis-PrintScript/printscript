package org.printscript.runner

import org.printscript.common.Version
import org.printscript.common.errorOrNull
import org.printscript.lexer.source.StringSourceReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// De punta a punta sobre readInput y readEnv: las dos primeras funciones del
// lenguaje que devuelven un valor y por lo tanto se usan adentro de expresiones.
class PrintScript11ReadTest {
    private fun correr(
        fuente: String,
        io: RecordingIO,
        version: Version = Version.V11,
    ) = ExecuteRunner(version, io).execute { StringSourceReader(fuente) }

    @Test
    fun `readInput imprime el prompt y devuelve lo que se ingreso`() {
        val io = RecordingIO(inputs = listOf("world"))

        val result =
            correr(
                """
                const name: string = readInput("Name:");
                println("Hello " + name + "!");
                """.trimIndent(),
                io,
            )

        assertNull(result.errorOrNull())
        assertEquals(listOf("Name:", "Hello world!"), io.output())
    }

    // El prompt sale de readInput, no del io: si lo imprimiera el io tambien,
    // aparecería dos veces.
    @Test
    fun `el prompt se imprime una sola vez`() {
        val io = RecordingIO(inputs = listOf("x"))

        correr("""let a: string = readInput("dame algo");""", io)

        assertEquals(listOf("dame algo"), io.output())
    }

    @Test
    fun `readEnv devuelve el valor de la variable`() {
        val io = RecordingIO(environment = mapOf("BEST_FOOTBALL_CLUB" to "San Lorenzo"))

        val result =
            correr(
                """
                const club: string = readEnv("BEST_FOOTBALL_CLUB");
                println(club);
                """.trimIndent(),
                io,
            )

        assertNull(result.errorOrNull())
        assertEquals(listOf("San Lorenzo"), io.output())
    }

    // No devuelve string vacio: un programa que lee una variable que nadie
    // definio esta roto y tiene que enterarse.
    @Test
    fun `readEnv de una variable que no existe falla`() {
        val error = correr("""const x: string = readEnv("NO_EXISTE");""", RecordingIO()).errorOrNull()

        assertNotNull(error)
        assertEquals("La variable de entorno 'NO_EXISTE' no está definida.", error.message)
    }

    @Test
    fun `readEnv necesita un string`() {
        val error = correr("const x: string = readEnv(5);", RecordingIO()).errorOrNull()

        assertNotNull(error)
        assertEquals("'readEnv' necesita un string y recibió '5'.", error.message)
    }

    @Test
    fun `readInput necesita un string`() {
        val error = correr("const x: string = readInput(5);", RecordingIO()).errorOrNull()

        assertNotNull(error)
        assertEquals("'readInput' necesita un string y recibió '5'.", error.message)
    }

    // Lo que hace falta para que "Hello " + readInput(...) funcione: la llamada
    // es una expresion mas, no un statement.
    @Test
    fun `readInput se puede usar en el medio de una expresion`() {
        val io = RecordingIO(inputs = listOf("mundo"))

        correr("""println("hola " + readInput("?"));""", io)

        assertEquals(listOf("?", "hola mundo"), io.output())
    }

    @Test
    fun `lo que se lee se puede asignar a una variable ya declarada`() {
        val io = RecordingIO(inputs = listOf("dos"))

        correr("""let a: string = "uno";a = readInput("?");println(a);""", io)

        assertEquals(listOf("?", "dos"), io.output())
    }

    // println no llega ni a ejecutarse en posicion de valor: solo readInput y
    // readEnv estan en callTokens, asi que para el parser un println ahi no es
    // una expresion valida. El error de ejecucion "no devuelve un valor" queda
    // igual como red --lo cubre ExpressionEvaluatorTest-- para el dia que haya
    // un built-in que no devuelva nada y si se pueda escribir aca.
    @Test
    fun `println no se puede usar como valor`() {
        val error = correr("""let a: string = println("hola");""", RecordingIO()).errorOrNull()

        assertNotNull(error)
        assertEquals("Se esperaba un valor, un identificador o '('", error.message)
    }

    // Lo que se lee es texto. Meterlo en un number no se convierte solo.
    @Test
    fun `lo que devuelve readInput es un string y no se convierte solo`() {
        val io = RecordingIO(inputs = listOf("5"))

        assertNotNull(correr("""let n: number = readInput("?");""", io).errorOrNull())
    }

    @Test
    fun `en 1_0 readInput y readEnv no existen`() {
        assertNotNull(correr("""let a: string = readInput("?");""", RecordingIO(), Version.V10).errorOrNull())
        assertNotNull(correr("""let a: string = readEnv("A");""", RecordingIO(), Version.V10).errorOrNull())
    }
}
