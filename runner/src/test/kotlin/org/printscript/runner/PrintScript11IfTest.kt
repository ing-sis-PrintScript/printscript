package org.printscript.runner

import org.printscript.common.Version
import org.printscript.common.errorOrNull
import org.printscript.lexer.source.StringSourceReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// De punta a punta sobre el if y el else de 1.1.
class PrintScript11IfTest {
    private val io = RecordingIO()

    private fun correr(
        fuente: String,
        version: Version = Version.V11,
    ) = ExecuteRunner(version, io).execute { StringSourceReader(fuente) }

    @Test
    fun `el bloque corre cuando la condicion es verdadera`() {
        val result =
            correr(
                """
                const activo: boolean = true;
                if(activo) {
                    println("adentro");
                }
                println("afuera");
                """.trimIndent(),
            )

        assertNull(result.errorOrNull())
        assertEquals(listOf("adentro", "afuera"), io.output())
    }

    @Test
    fun `el bloque no corre cuando la condicion es falsa`() {
        correr(
            """
            const activo: boolean = false;
            if(activo) {
                println("adentro");
            }
            println("afuera");
            """.trimIndent(),
        )

        assertEquals(listOf("afuera"), io.output())
    }

    @Test
    fun `con la condicion falsa corre el else`() {
        correr(
            """
            const activo: boolean = false;
            if(activo) {
                println("then");
            } else {
                println("else");
            }
            """.trimIndent(),
        )

        assertEquals(listOf("else"), io.output())
    }

    @Test
    fun `con la condicion verdadera el else no corre`() {
        correr(
            """
            const activo: boolean = true;
            if(activo) {
                println("then");
            } else {
                println("else");
            }
            """.trimIndent(),
        )

        assertEquals(listOf("then"), io.output())
    }

    @Test
    fun `el bloque puede tener varios statements y ejecutarlos en orden`() {
        correr(
            """
            if(true) {
                println("uno");
                let x: number = 2;
                println(x);
            }
            """.trimIndent(),
        )

        assertEquals(listOf("uno", "2"), io.output())
    }

    @Test
    fun `un bloque vacio es valido`() {
        val result = correr("if(true) {}println(\"sigo\");")

        assertNull(result.errorOrNull())
        assertEquals(listOf("sigo"), io.output())
    }

    @Test
    fun `un if adentro de otro if`() {
        correr(
            """
            if(true) {
                if(false) {
                    println("no");
                } else {
                    println("si");
                }
            }
            """.trimIndent(),
        )

        assertEquals(listOf("si"), io.output())
    }

    // El parser acepta cualquier expresion como condicion; que sea boolean se
    // controla al ejecutar.
    @Test
    fun `la condicion tiene que ser un boolean`() {
        val error = correr("if(5) { println(\"no\"); }").errorOrNull()

        assertNotNull(error)
        assertEquals("La condición de un if tiene que ser un boolean.", error.message)
    }

    // Las dos mitades del scope de bloque.
    @Test
    fun `una variable declarada adentro del bloque no existe afuera`() {
        val error =
            correr(
                """
                if(true) {
                    let interna: number = 5;
                }
                println(interna);
                """.trimIndent(),
            ).errorOrNull()

        assertNotNull(error)
        assertEquals("La variable 'interna' no ha sido declarada.", error.message)
    }

    @Test
    fun `asignarle a una variable de afuera si queda despues del bloque`() {
        correr(
            """
            let x: number = 1;
            if(true) {
                x = 2;
            }
            println(x);
            """.trimIndent(),
        )

        assertEquals(listOf("2"), io.output())
    }

    @Test
    fun `si falta la llave que cierra es error`() {
        assertNotNull(correr("if(true) { println(\"a\");").errorOrNull())
    }

    @Test
    fun `si falta el parentesis que cierra es error`() {
        assertNotNull(correr("if(true { println(\"a\"); }").errorOrNull())
    }

    // El if no existe en 1.0: ahi 'if' sale como IDENTIFIER y no lo agarra
    // ningun parser de statement.
    @Test
    fun `el if corrido como 1_0 falla`() {
        assertNotNull(correr("if(true) { println(\"a\"); }", Version.V10).errorOrNull())
    }
}
