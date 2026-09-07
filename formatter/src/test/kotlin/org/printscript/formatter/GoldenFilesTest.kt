package org.printscript.formatter

import org.printscript.ast.BinaryOperator.DIVIDE
import org.printscript.ast.BinaryOperator.MINUS
import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.ast.BinaryOperator.TIMES
import org.printscript.ast.DeclaredType.NUMBER
import org.printscript.ast.DeclaredType.STRING
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.formatter.expressions.binary
import org.printscript.formatter.expressions.call
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import org.printscript.formatter.expressions.string
import org.printscript.formatter.statements.assignment
import org.printscript.formatter.statements.declaration
import org.printscript.formatter.statements.expressionStatement
import kotlin.test.Test
import kotlin.test.assertEquals

class GoldenFilesTest {
    private val withBlankLine = FormatterConfig(blankLinesBeforePrintln = BlankLines.ONE)

    private fun golden(name: String): String =
        checkNotNull(javaClass.getResourceAsStream("/golden/$name")) { "falta el golden $name" }
            .bufferedReader()
            .readText()

    private fun greeting() = binary(PLUS, binary(PLUS, id("name"), string(" ")), id("lastName"))

    private fun example1() =
        program(
            declaration("name", STRING, string("Joe")),
            declaration("lastName", STRING, string("Doe")),
            expressionStatement(call("println", greeting())),
        )

    @Test
    fun `ejemplo 1`() {
        assertEquals(golden("ejemplo1.ps"), formatToText(example1(), withBlankLine))
    }

    @Test
    fun `ejemplo 2`() {
        val program =
            program(
                declaration("a", NUMBER, number(12.0)),
                declaration("b", NUMBER, number(4.0)),
                declaration("c", NUMBER, binary(DIVIDE, id("a"), id("b"))),
                expressionStatement(call("println", binary(PLUS, string("Result: "), id("c")))),
            )

        assertEquals(golden("ejemplo2.ps"), formatToText(program, withBlankLine))
    }

    @Test
    fun `ejemplo 3 con una asignacion`() {
        val program =
            program(
                declaration("a", NUMBER, number(12.0)),
                declaration("b", NUMBER, number(4.0)),
                assignment("a", binary(DIVIDE, id("a"), id("b"))),
                expressionStatement(call("println", binary(PLUS, string("Result: "), id("a")))),
            )

        assertEquals(golden("ejemplo3.ps"), formatToText(program, withBlankLine))
    }

    @Test
    fun `ejemplo 1 con las tres reglas de espaciado apagadas`() {
        val config =
            withBlankLine.copy(
                spaceBeforeColon = Spacing.NONE,
                spaceAfterColon = Spacing.NONE,
                spaceAroundAssignment = Spacing.NONE,
            )

        assertEquals(golden("sin-espacios.ps"), formatToText(example1(), config))
    }

    @Test
    fun `ejemplo 1 con espacio antes de los dos puntos`() {
        val config = withBlankLine.copy(spaceBeforeColon = Spacing.SINGLE)

        assertEquals(golden("espacio-antes-de-dos-puntos.ps"), formatToText(example1(), config))
    }

    @Test
    fun `los parentesis necesarios sobreviven al formateo`() {
        val program =
            program(
                declaration("x", NUMBER, binary(TIMES, binary(PLUS, number(1.0), number(2.0)), number(3.0))),
                declaration("y", NUMBER, binary(MINUS, id("a"), binary(MINUS, id("b"), id("c")))),
                declaration("z", NUMBER, binary(PLUS, id("a"), binary(PLUS, id("b"), id("c")))),
            )

        assertEquals(golden("parentesis.ps"), formatToText(program, FormatterConfig()))
    }
}
