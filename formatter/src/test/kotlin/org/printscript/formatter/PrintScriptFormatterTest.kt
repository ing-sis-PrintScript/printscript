package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.ast.DeclaredType.NUMBER
import org.printscript.ast.DeclaredType.STRING
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.errorOrNull
import org.printscript.common.getOrNull
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.expressions.ANY_RANGE
import org.printscript.formatter.expressions.PrintScript10ExpressionFormatter
import org.printscript.formatter.expressions.binary
import org.printscript.formatter.expressions.call
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import org.printscript.formatter.expressions.string
import org.printscript.formatter.statements.AssignmentFormatter
import org.printscript.formatter.statements.DeclarationFormatter
import org.printscript.formatter.statements.ExpressionStatementFormatter
import org.printscript.formatter.statements.PrintScript10StatementDispatcher
import org.printscript.formatter.statements.assignment
import org.printscript.formatter.statements.declaration
import org.printscript.formatter.statements.expressionStatement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data class BrokenSource(
    override val message: String,
    override val range: Range = ANY_RANGE,
) : PrintScriptError

class PrintScriptFormatterTest {
    private fun printScript10Formatters(): List<PartialNodeFormatter<ASTNode>> {
        val expressions = PrintScript10ExpressionFormatter()
        return listOf(
            PrintScript10StatementDispatcher(
                declarations = DeclarationFormatter(expressions),
                assignments = AssignmentFormatter(expressions),
                expressionStatements = ExpressionStatementFormatter(expressions),
            ),
        )
    }

    private fun formatter(config: FormatterConfig): PrintScriptFormatter =
        PrintScriptFormatter(printScript10Formatters(), StatementSeparator(), config)

    private fun format(
        program: Sequence<Result<ASTNode, PrintScriptError>>,
        config: FormatterConfig = FormatterConfig(),
    ): List<Result<FormattedCode, PrintScriptError>> = formatter(config).format(program).toList()

    private fun textOf(results: List<Result<FormattedCode, PrintScriptError>>): String =
        results.mapNotNull { it.getOrNull() }.joinToString("") { it.text }

    private fun ok(node: ASTNode): Result<ASTNode, PrintScriptError> = Result.Success(node)

    private fun broken(message: String): Result<ASTNode, PrintScriptError> = Result.Failure(BrokenSource(message))

    @Test
    fun `un programa vacio devuelve una secuencia vacia`() {
        assertTrue(format(emptySequence()).isEmpty())
    }

    @Test
    fun `una sola sentencia sale con su salto de linea final`() {
        val program = sequenceOf(ok(declaration("x", NUMBER, number(5.0))))

        assertEquals("let x: number = 5;\n", textOf(format(program)))
    }

    @Test
    fun `cada sentencia queda en su propia linea`() {
        val program =
            sequenceOf(
                ok(declaration("x", NUMBER, number(1.0))),
                ok(assignment("x", number(2.0))),
                ok(expressionStatement(call("printSomething", id("x")))),
            )

        assertEquals("let x: number = 1;\nx = 2;\nprintSomething(x);\n", textOf(format(program)))
    }

    @Test
    fun `cada sentencia sale como un elemento propio de la secuencia`() {
        val program =
            sequenceOf(
                ok(declaration("x", NUMBER, number(1.0))),
                ok(assignment("x", number(2.0))),
            )

        assertEquals(2, format(program).size)
    }

    @Test
    fun `con una linea en blanco antes del println queda una linea vacia en el medio`() {
        val program =
            sequenceOf(
                ok(declaration("x", NUMBER, number(1.0))),
                ok(expressionStatement(call("println", id("x")))),
            )
        val config = FormatterConfig(blankLinesBeforePrintln = BlankLines.ONE)

        assertEquals("let x: number = 1;\n\nprintln(x);\n", textOf(format(program, config)))
    }

    @Test
    fun `un println como primera sentencia no arranca con lineas en blanco`() {
        val program = sequenceOf(ok(expressionStatement(call("println", id("x")))))
        val config = FormatterConfig(blankLinesBeforePrintln = BlankLines.TWO)

        assertEquals("println(x);\n", textOf(format(program, config)))
    }

    @Test
    fun `un nodo que ningun formatter reconoce se reporta como no soportado`() {
        val node = binary(PLUS, id("a"), id("b"))

        val results = format(sequenceOf(ok(node)))

        assertEquals(1, results.size)
        assertEquals(UnsupportedNode(node), results.single().errorOrNull())
    }

    @Test
    fun `el primer formatter que reconoce el nodo gana`() {
        val extension = PartialNodeFormatter<ASTNode> { _, _ -> FormattedCode("de otra version") }
        val nodes = listOf(extension) + printScript10Formatters()
        val formatter = PrintScriptFormatter(nodes, StatementSeparator(), FormatterConfig())
        val program = sequenceOf(ok(declaration("x", NUMBER, number(1.0))))

        assertEquals("de otra version\n", textOf(formatter.format(program).toList()))
    }

    @Test
    fun `un nodo no soportado corta la secuencia`() {
        val program =
            sequenceOf(
                ok(declaration("x", NUMBER, number(1.0))),
                ok(binary(PLUS, id("a"), id("b"))),
                ok(assignment("x", number(2.0))),
            )

        val results = format(program)

        assertEquals(2, results.size)
        assertEquals("let x: number = 1;\n", textOf(results))
    }

    @Test
    fun `un fallo se emite y corta la secuencia`() {
        val program =
            sequenceOf(
                ok(declaration("x", NUMBER, number(1.0))),
                broken("no se esperaba ese token"),
                ok(assignment("x", number(2.0))),
            )

        val results = format(program)

        assertEquals(2, results.size)
        assertEquals("let x: number = 1;\n", textOf(results))
        assertEquals("no se esperaba ese token", results.last().errorOrNull()?.message)
    }

    @Test
    fun `un fallo como primer elemento no emite nada formateado`() {
        val program =
            sequenceOf(
                broken("no se esperaba ese token"),
                ok(declaration("x", NUMBER, number(1.0))),
            )

        val results = format(program)

        assertEquals(1, results.size)
        assertEquals("", textOf(results))
    }

    @Test
    fun `la salida sigue siendo perezosa frente a una entrada infinita`() {
        val infinite = generateSequence { ok(expressionStatement(call("println", id("x")))) }

        val firstThree = formatter(FormatterConfig()).format(infinite).take(3).toList()

        assertEquals(3, firstThree.size)
    }

    @Test
    fun `el ejemplo del enunciado sale formateado completo`() {
        val greeting = binary(PLUS, binary(PLUS, id("name"), string(" ")), id("lastName"))
        val program =
            sequenceOf(
                ok(declaration("name", STRING, string("Joe"))),
                ok(declaration("lastName", STRING, string("Doe"))),
                ok(expressionStatement(call("println", greeting))),
            )
        val config = FormatterConfig(blankLinesBeforePrintln = BlankLines.ONE)

        val expected =
            "let name: string = \"Joe\";\n" +
                "let lastName: string = \"Doe\";\n" +
                "\n" +
                "println(name + \" \" + lastName);\n"

        assertEquals(expected, textOf(format(program, config)))
    }
}
