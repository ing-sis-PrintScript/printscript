package org.printscript.formatter.statements

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.ast.BinaryOperator.TIMES
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.formatter.expressions.PrintScript10ExpressionFormatter
import org.printscript.formatter.expressions.binary
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import kotlin.test.Test
import kotlin.test.assertEquals

class AssignmentFormatterTest {
    private val formatter = AssignmentFormatter(PrintScript10ExpressionFormatter())

    private fun format(
        node: AssignmentStatement,
        config: FormatterConfig = FormatterConfig(),
    ): String = formatter.format(node, FormatterContext(config)).text

    @Test
    fun `una asignacion usa la config por defecto`() {
        assertEquals("x = 5;", format(assignment("x", number(5.0))))
    }

    @Test
    fun `sin espacio alrededor del igual`() {
        val config = FormatterConfig(spaceAroundAssignment = Spacing.NONE)

        assertEquals("x=5;", format(assignment("x", number(5.0)), config))
    }

    @Test
    fun `el valor se delega al formatter de expresiones`() {
        val value = binary(TIMES, id("a"), binary(PLUS, id("b"), id("c")))

        assertEquals("x = a * (b + c);", format(assignment("x", value)))
    }
}
