package org.printscript.formatter

import org.printscript.ast.DeclaredType.NUMBER
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.expressions.call
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import org.printscript.formatter.statements.declaration
import org.printscript.formatter.statements.expressionStatement
import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScript10Test {
    @Test
    fun `el factory arma un formatter que formatea de punta a punta`() {
        val program =
            program(
                declaration("x", NUMBER, number(5.0)),
                expressionStatement(call("println", id("x"))),
            )
        val config = FormatterConfig(blankLinesBeforePrintln = BlankLines.ONE)

        assertEquals("let x: number = 5;\n\nprintln(x);\n", formatToText(program, config))
    }
}
