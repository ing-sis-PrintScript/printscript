package org.printscript.cli.runners

import org.printscript.lexer.source.StringSourceReader
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateRunnerTest {
    private fun validate(source: String) = ValidateRunner().validate(StringSourceReader(source))

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
}
