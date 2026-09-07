package org.printscript.runner

import org.printscript.lexer.source.StringSourceReader
import org.printscript.runner.progress.Progress
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateRunnerTest {
    private fun validate(source: String) = ValidateRunner().validate { StringSourceReader(source) }

    @Test
    fun `un archivo sano no reporta errores`() {
        val errors =
            validate(
                """
                let name: string = "Joe";
                println(name);
                """.trimIndent(),
            )

        assertEquals(emptyList(), errors)
    }

    @Test
    fun `reporta todos los errores de sintaxis en una sola pasada`() {
        val errors =
            validate(
                """
                let x: number = ;
                let y: number = 3;
                let z: string = ;
                """.trimIndent(),
            )

        assertEquals(2, errors.size, "un validador tiene que reportar todos, no el primero")
    }

    @Test
    fun `cada error trae su linea`() {
        val errors =
            validate(
                """
                let x: number = ;
                let y: number = 3;
                let z: string = ;
                """.trimIndent(),
            )

        assertEquals(listOf(1, 3), errors.map { it.range.start.line })
    }

    @Test
    fun `avisa una vez por cada sentencia parseada`() {
        var notices = 0

        ValidateRunner(Progress { notices++ })
            .validate { StringSourceReader("let x: number = 1;\nlet y: number = 2;") }

        assertEquals(2, notices)
    }
}
