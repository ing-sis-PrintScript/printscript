package org.printscript.runner

import org.printscript.common.Version
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.lexer.source.LineReadResult
import org.printscript.lexer.source.SourceReader
import org.printscript.lexer.source.StreamSourceReader
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

private const val LINES = 32 * 1024
private const val PROGRAMA = "println(\"hola\");\n"

// Los tres tests suben una capa por vez sobre la misma fuente y el mismo heap, para
// que cuando algo no entre en memoria se sepa DONDE y no solo QUE.
class LargeProgramMemoryTest {
    // Replica el MockInputStream del TCK: entrega los bytes de la misma linea N veces
    // sin tener el programa entero en memoria. Asi lo unico que puede acumularse es
    // lo que retenga el pipeline, que es lo que se esta midiendo.
    private class RepeatedLineStream(
        line: String,
        private val times: Int,
    ) : InputStream() {
        private val bytes = line.toByteArray()

        // Dos var, y las dos hacen falta: una es la posicion dentro de la linea, la
        // otra cuantas lineas van. Es el estado minimo de un stream.
        private var index = 0
        private var emitted = 0

        override fun read(): Int {
            if (index == bytes.size) {
                index = 0
                emitted++
            }

            if (emitted == times) return -1

            return bytes[index++].toInt()
        }
    }

    // Cuenta y descarta, como el PrintCounter del TCK: el consumo de memoria tiene
    // que depender del pipeline, no de lo que el IO decida guardar.
    private class CountingIO : PrintScriptIO {
        // El unico var de la clase, y es lo que se esta midiendo.
        var printed = 0
            private set

        override fun print(message: String) {
            printed++
        }

        override fun read(prompt: String): String = ""
    }

    private fun fuente(): SourceReader = StreamSourceReader.of(RepeatedLineStream(PROGRAMA, LINES))

    // Se atrapa el OutOfMemoryError por la misma razon que lo atrapa el adaptador del
    // TCK: sin esto el error se lleva puesto al proceso de test y no queda registro.
    // El contador dice cuantas lineas se alcanzaron, que es el dato que orienta.
    private fun sinQuedarseSinMemoria(
        capa: String,
        alcanzadas: () -> Int,
        correr: () -> Unit,
    ) {
        try {
            correr()
        } catch (error: OutOfMemoryError) {
            fail("$capa: sin memoria a las ${alcanzadas()} lineas de $LINES (${error.message})")
        }

        assertEquals(LINES, alcanzadas())
    }

    @Test
    fun `1 - leer 32K lineas`() {
        // var y no val: cada paso pisa el eslabon anterior, que es justamente lo que
        // permite que el consumido quede sin apuntadores.
        var reader = fuente()
        var lineas = 0

        sinQuedarseSinMemoria("reader", { lineas }) {
            while (true) {
                when (val read = reader.nextLine()) {
                    is LineReadResult.Success -> {
                        lineas++
                        reader = read.remaining
                    }

                    LineReadResult.EndOfInput -> return@sinQuedarseSinMemoria
                }
            }
        }
    }

    @Test
    fun `2 - lexear y parsear 32K lineas`() {
        var parsed = 0

        sinQuedarseSinMemoria("lexer + parser", { parsed }) {
            statements({ fuente() }, Version.V10).forEach { parsed++ }
        }
    }

    @Test
    fun `3 - ejecutar 32K lineas`() {
        val io = CountingIO()

        sinQuedarseSinMemoria("ejecucion completa", { io.printed }) {
            ExecuteRunner(Version.V10, io).execute { fuente() }
        }
    }
}
