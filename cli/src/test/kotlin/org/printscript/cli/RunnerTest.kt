package org.printscript.cli

import org.printscript.common.errorOrNull
import org.printscript.lexer.source.StringSourceReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RunnerTest {
    private val io = RecordingIO()

    private fun run(source: String) = Runner(io).execute(StringSourceReader(source))

    @Test
    fun `ejemplo 1 de la consigna`() {
        val result =
            run(
                """
                let name: string = "Joe";
                let lastName: string = "Doe";
                println(name + " " + lastName);
                """.trimIndent(),
            )

        assertNull(result.errorOrNull())
        assertEquals(listOf("Joe Doe"), io.output())
    }

    @Test
    fun `ejemplo 2 de la consigna`() {
        val result =
            run(
                """
                let a: number = 12;
                let b: number = 4;
                let c: number = a / b;
                println("Result: " + c);
                """.trimIndent(),
            )

        assertNull(result.errorOrNull())
        assertEquals(listOf("Result: 3"), io.output())
    }

    @Test
    fun `un error corta la ejecucion y trae la posicion`() {
        val result = run("""let x: number = @;""")
        val error = assertNotNull(result.errorOrNull())

        assertEquals("Caracter inesperado '@'", error.message)
        assertEquals(17, error.range.start.column)
    }
}
