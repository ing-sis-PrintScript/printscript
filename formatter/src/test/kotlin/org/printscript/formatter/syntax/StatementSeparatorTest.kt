package org.printscript.formatter.syntax

import org.printscript.ast.DeclaredType.NUMBER
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.expressions.call
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import org.printscript.formatter.statements.declaration
import org.printscript.formatter.statements.expressionStatement
import kotlin.test.Test
import kotlin.test.assertEquals

class StatementSeparatorTest {
    private val separator = StatementSeparator()

    private fun context(blankLines: BlankLines = BlankLines.NONE) =
        FormatterContext(FormatterConfig(blankLinesBeforePrintln = blankLines))

    private val printlnCall = expressionStatement(call("println", id("x")))
    private val declaration = declaration("x", NUMBER, number(5.0))

    @Test
    fun `after devuelve siempre un salto de linea`() {
        assertEquals("\n", separator.after().text)
    }

    @Test
    fun `antes de la primera sentencia nunca hay lineas en blanco`() {
        val before = separator.before(isFirst = true, node = printlnCall, context = context(BlankLines.TWO))

        assertEquals("", before.text)
    }

    @Test
    fun `con cero lineas en blanco el println no lleva nada adelante`() {
        val before = separator.before(isFirst = false, node = printlnCall, context = context(BlankLines.NONE))

        assertEquals("", before.text)
    }

    @Test
    fun `con una linea en blanco el println lleva un salto adelante`() {
        val before = separator.before(isFirst = false, node = printlnCall, context = context(BlankLines.ONE))

        assertEquals("\n", before.text)
    }

    @Test
    fun `con dos lineas en blanco el println lleva dos saltos adelante`() {
        val before = separator.before(isFirst = false, node = printlnCall, context = context(BlankLines.TWO))

        assertEquals("\n\n", before.text)
    }

    @Test
    fun `una sentencia que no es println nunca lleva lineas en blanco adelante`() {
        assertEquals("", separator.before(isFirst = false, node = declaration, context = context(BlankLines.TWO)).text)
        assertEquals("", separator.before(isFirst = false, node = declaration, context = context(BlankLines.NONE)).text)
    }

    @Test
    fun `una llamada a otra funcion no lleva lineas en blanco adelante`() {
        val node = expressionStatement(call("print", id("x")))

        assertEquals("", separator.before(isFirst = false, node = node, context = context(BlankLines.TWO)).text)
    }

    @Test
    fun `una sentencia de expresion que no es una llamada no lleva lineas en blanco adelante`() {
        val node = expressionStatement(id("x"))

        assertEquals("", separator.before(isFirst = false, node = node, context = context(BlankLines.TWO)).text)
    }

    @Test
    fun `una llamada suelta sin envolver en una sentencia no lleva lineas en blanco adelante`() {
        val node = call("println", id("x"))

        assertEquals("", separator.before(isFirst = false, node = node, context = context(BlankLines.TWO)).text)
    }
}
