package org.printscript.formatter.statements

import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.ast.ExpressionStatement
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.expressions.PrintScript10ExpressionFormatter
import org.printscript.formatter.expressions.binary
import org.printscript.formatter.expressions.call
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.string
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpressionStatementFormatterTest {
    private val formatter = ExpressionStatementFormatter(PrintScript10ExpressionFormatter())

    private fun format(node: ExpressionStatement): String =
        formatter.format(node, FormatterContext(FormatterConfig())).text

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
    fun `la salida termina en punto y coma y no incluye saltos de linea`() {
        val formatted = format(expressionStatement(call("println", id("x"))))

        assertTrue(formatted.endsWith(";"))
        assertFalse(formatted.contains("\n"))
    }
}
