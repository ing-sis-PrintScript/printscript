package org.printscript.formatter.statements

import org.printscript.ast.ASTNode
import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.ast.BinaryOperator.TIMES
import org.printscript.ast.DeclaredType.NUMBER
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.formatter.expressions.PrintScript10ExpressionFormatter
import org.printscript.formatter.expressions.binary
import org.printscript.formatter.expressions.call
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import org.printscript.formatter.expressions.string
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrintScript10StatementDispatcherTest {
    private val expressions = PrintScript10ExpressionFormatter()
    private val dispatcher = PrintScript10StatementDispatcher(DeclarationFormatter(expressions), expressions)

    private fun format(
        node: ASTNode,
        config: FormatterConfig = FormatterConfig(),
    ): String? = dispatcher.formatOrNull(node, FormatterContext(config))?.text

    @Test
    fun `una declaracion va al formatter de declaraciones`() {
        assertEquals("let x: number = 5;", format(declaration("x", NUMBER, number(5.0))))
    }

    @Test
    fun `una asignacion usa la config por defecto`() {
        assertEquals("x = 5;", format(assignment("x", number(5.0))))
    }

    @Test
    fun `una asignacion sin espacio alrededor del igual`() {
        val config = FormatterConfig(spaceAroundAssignment = Spacing.NONE)

        assertEquals("x=5;", format(assignment("x", number(5.0)), config))
    }

    @Test
    fun `el valor de una asignacion se delega al formatter de expresiones`() {
        val value = binary(TIMES, id("a"), binary(PLUS, id("b"), id("c")))

        assertEquals("x = a * (b + c);", format(assignment("x", value)))
    }

    @Test
    fun `un println sale como la llamada terminada en punto y coma`() {
        assertEquals("println(x);", format(expressionStatement(call("println", id("x")))))
    }

    @Test
    fun `un println con concatenacion delega en el formatter de expresiones`() {
        val expression = call("println", binary(PLUS, string("Result: "), id("c")))

        assertEquals("println(\"Result: \" + c);", format(expressionStatement(expression)))
    }

    @Test
    fun `una sentencia de expresion termina en punto y coma y no incluye saltos de linea`() {
        val formatted = checkNotNull(format(expressionStatement(call("println", id("x")))))

        assertTrue(formatted.endsWith(";"))
        assertFalse(formatted.contains("\n"))
    }

    @Test
    fun `un nodo que no es una sentencia de PrintScript 1_0 no lo reconoce`() {
        assertNull(format(binary(PLUS, id("a"), id("b"))))
    }
}
