package org.printscript.formatter.statements

import org.printscript.ast.ASTNode
import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.ast.DeclaredType.NUMBER
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.NodeFormatter
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.expressions.binary
import org.printscript.formatter.expressions.call
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PrintScript10StatementDispatcherTest {
    private fun marker(mark: String) = NodeFormatter<ASTNode> { _, _ -> FormattedCode(mark) }

    private val dispatcher =
        PrintScript10StatementDispatcher(
            declarations = marker("declaration"),
            assignments = marker("assignment"),
            expressionStatements = marker("expression statement"),
        )

    private fun dispatch(node: ASTNode): FormattedCode? =
        dispatcher.formatOrNull(node, FormatterContext(FormatterConfig()))

    @Test
    fun `una declaracion va al formatter de declaraciones`() {
        assertEquals(FormattedCode("declaration"), dispatch(declaration("x", NUMBER, number(5.0))))
    }

    @Test
    fun `una asignacion va al formatter de asignaciones`() {
        assertEquals(FormattedCode("assignment"), dispatch(assignment("x", number(5.0))))
    }

    @Test
    fun `una sentencia de expresion va al formatter de sentencias de expresion`() {
        assertEquals(FormattedCode("expression statement"), dispatch(expressionStatement(call("println", id("x")))))
    }

    @Test
    fun `un nodo que no es una sentencia de PrintScript 1_0 no lo reconoce`() {
        assertNull(dispatch(binary(PLUS, id("a"), id("b"))))
    }

    @Test
    fun `el contexto llega intacto al formatter elegido`() {
        val config = FormatterConfig()
        val echo = NodeFormatter<ASTNode> { _, context -> FormattedCode(context.config.toString()) }
        val dispatcher = PrintScript10StatementDispatcher(echo, echo, echo)

        val formatted = dispatcher.formatOrNull(assignment("x", number(5.0)), FormatterContext(config))

        assertEquals(FormattedCode(config.toString()), formatted)
    }
}
