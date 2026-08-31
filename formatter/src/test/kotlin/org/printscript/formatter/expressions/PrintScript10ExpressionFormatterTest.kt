package org.printscript.formatter.expressions

import org.printscript.ast.BinaryOperator.DIVIDE
import org.printscript.ast.BinaryOperator.MINUS
import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.ast.BinaryOperator.TIMES
import org.printscript.ast.Expression
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.config.FormatterConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScript10ExpressionFormatterTest {
    private val formatter = PrintScript10ExpressionFormatter()
    private val context = FormatterContext(FormatterConfig())

    private fun format(expression: Expression): String = formatter.format(expression, context).text

    @Test
    fun `un literal numerico entero no arrastra decimales`() {
        assertEquals("5", format(number(5.0)))
    }

    @Test
    fun `un literal de texto sale entre comillas`() {
        assertEquals("\"hola\"", format(string("hola")))
    }

    @Test
    fun `un identificador sale con su nombre`() {
        assertEquals("x", format(id("x")))
    }

    @Test
    fun `todo operador binario va rodeado de un unico espacio`() {
        assertEquals("a + b", format(binary(PLUS, id("a"), id("b"))))
        assertEquals("a - b", format(binary(MINUS, id("a"), id("b"))))
        assertEquals("a * b", format(binary(TIMES, id("a"), id("b"))))
        assertEquals("a / b", format(binary(DIVIDE, id("a"), id("b"))))
    }

    @Test
    fun `un producto anidado a la derecha de una suma no lleva parentesis`() {
        val tree = binary(PLUS, number(1.0), binary(TIMES, number(2.0), number(3.0)))

        assertEquals("1 + 2 * 3", format(tree))
    }

    @Test
    fun `una suma anidada a la izquierda de un producto lleva parentesis`() {
        val tree = binary(TIMES, binary(PLUS, number(1.0), number(2.0)), number(3.0))

        assertEquals("(1 + 2) * 3", format(tree))
    }

    @Test
    fun `una resta asociada a la izquierda no lleva parentesis`() {
        val tree = binary(MINUS, binary(MINUS, id("a"), id("b")), id("c"))

        assertEquals("a - b - c", format(tree))
    }

    @Test
    fun `una resta asociada a la derecha lleva parentesis`() {
        val tree = binary(MINUS, id("a"), binary(MINUS, id("b"), id("c")))

        assertEquals("a - (b - c)", format(tree))
    }

    @Test
    fun `una suma anidada a la derecha conserva los parentesis`() {
        val tree = binary(PLUS, id("a"), binary(PLUS, id("b"), id("c")))

        assertEquals("a + (b + c)", format(tree))
    }

    @Test
    fun `una division anidada a la derecha conserva los parentesis`() {
        val tree = binary(DIVIDE, id("a"), binary(DIVIDE, id("b"), id("c")))

        assertEquals("a / (b / c)", format(tree))
    }

    @Test
    fun `una llamada con un argumento`() {
        assertEquals("println(x)", format(call("println", id("x"))))
    }

    @Test
    fun `una llamada con dos argumentos los separa con coma y espacio`() {
        assertEquals("max(a, 2)", format(call("max", id("a"), number(2.0))))
    }

    @Test
    fun `una llamada sin argumentos`() {
        assertEquals("now()", format(call("now")))
    }

    @Test
    fun `una concatenacion con un literal de texto`() {
        val tree = binary(PLUS, string("Result: "), id("c"))

        assertEquals("\"Result: \" + c", format(tree))
    }

    @Test
    fun `arboles distintos nunca producen el mismo texto`() {
        val trees =
            listOf(
                binary(PLUS, number(1.0), binary(TIMES, number(2.0), number(3.0))),
                binary(TIMES, binary(PLUS, number(1.0), number(2.0)), number(3.0)),
                binary(MINUS, binary(MINUS, id("a"), id("b")), id("c")),
                binary(MINUS, id("a"), binary(MINUS, id("b"), id("c"))),
                binary(PLUS, binary(PLUS, id("a"), id("b")), id("c")),
                binary(PLUS, id("a"), binary(PLUS, id("b"), id("c"))),
                binary(DIVIDE, binary(DIVIDE, id("a"), id("b")), id("c")),
                binary(DIVIDE, id("a"), binary(DIVIDE, id("b"), id("c"))),
            )

        val formatted = trees.map { format(it) }

        assertEquals(trees.size, formatted.toSet().size, "dos arboles distintos colapsaron al mismo texto")
    }
}
