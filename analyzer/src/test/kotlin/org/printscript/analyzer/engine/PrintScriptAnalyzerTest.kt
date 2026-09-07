package org.printscript.analyzer.engine

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.Severity
import org.printscript.analyzer.binary
import org.printscript.analyzer.call
import org.printscript.analyzer.collectDiagnostics
import org.printscript.analyzer.declaration
import org.printscript.analyzer.expressionStatement
import org.printscript.analyzer.id
import org.printscript.analyzer.number
import org.printscript.analyzer.program
import org.printscript.analyzer.programWithSyntaxError
import org.printscript.analyzer.rangeAt
import org.printscript.ast.ASTNode
import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.VariableDeclaration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScriptAnalyzerTest {
    /** Una regla de prueba que reporta cualquier VariableDeclaration, sin importar el nombre. */
    private object FlagsEveryDeclaration : Rule {
        override fun check(
            node: ASTNode,
            emit: DiagnosticEmitter,
        ) {
            if (node is VariableDeclaration) {
                emit.emit(Diagnostic("flag-declaration", "marcado", node.range, Severity.WARNING))
            }
        }
    }

    /** Una regla de prueba que reporta cualquier BinaryExpression que encuentre, a cualquier profundidad. */
    private object FlagsEveryBinary : Rule {
        override fun check(
            node: ASTNode,
            emit: DiagnosticEmitter,
        ) {
            if (node is BinaryExpression) {
                emit.emit(Diagnostic("flag-binary", "marcado", node.range, Severity.WARNING))
            }
        }
    }

    @Test
    fun `un programa con varios problemas reporta todos, no solo el primero`() {
        val analyzer = PrintScriptAnalyzer(listOf(FlagsEveryDeclaration))
        val statements =
            program(
                declaration("a"),
                declaration("b"),
                declaration("c"),
            )

        val diagnostics = analyzer.collectDiagnostics(statements)

        assertEquals(3, diagnostics.size)
    }

    @Test
    fun `baja recursivamente y encuentra un problema anidado dentro de una expresion`() {
        val analyzer = PrintScriptAnalyzer(listOf(FlagsEveryBinary))
        val inner = binary(BinaryOperator.PLUS, number(1.0), number(2.0))
        val outer = binary(BinaryOperator.TIMES, inner, number(3.0))
        val statements = program(expressionStatement(outer))

        val diagnostics = analyzer.collectDiagnostics(statements)

        // Encuentra las DOS BinaryExpression: la de afuera Y la anidada adentro.
        assertEquals(2, diagnostics.size)
    }

    @Test
    fun `baja dentro de los argumentos de una CallExpression`() {
        val analyzer = PrintScriptAnalyzer(listOf(FlagsEveryBinary))
        val argument = binary(BinaryOperator.PLUS, number(1.0), number(2.0))
        val statements = program(expressionStatement(call("println", argument)))

        val diagnostics = analyzer.collectDiagnostics(statements)

        assertEquals(1, diagnostics.size)
    }

    @Test
    fun `reporta la posicion exacta de cada nodo, no una posicion generica`() {
        val analyzer = PrintScriptAnalyzer(listOf(FlagsEveryDeclaration))
        val rangeA = rangeAt(line = 1, column = 5, length = 1)
        val rangeB = rangeAt(line = 4, column = 9, length = 1)
        val statements = program(declaration("a").copy(range = rangeA), declaration("b").copy(range = rangeB))

        val diagnostics = analyzer.collectDiagnostics(statements)

        assertEquals(setOf(rangeA, rangeB), diagnostics.map { it.range }.toSet())
    }

    @Test
    fun `un error de sintaxis intercalado no corta el analisis del resto del archivo`() {
        val analyzer = PrintScriptAnalyzer(listOf(FlagsEveryDeclaration))
        val statements = programWithSyntaxError(declaration("a"), declaration("b"))

        val diagnostics = analyzer.collectDiagnostics(statements)

        assertEquals(2, diagnostics.size)
    }

    @Test
    fun `sin reglas registradas no reporta nada, pero tampoco falla`() {
        val analyzer = PrintScriptAnalyzer(emptyList())
        val statements = program(declaration("a"), expressionStatement(call("println", id("a"))))

        assertTrue(analyzer.collectDiagnostics(statements).isEmpty())
    }
}
