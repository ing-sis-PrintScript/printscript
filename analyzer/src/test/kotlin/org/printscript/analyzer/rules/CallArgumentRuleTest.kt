package org.printscript.analyzer.rules

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.binary
import org.printscript.analyzer.call
import org.printscript.analyzer.expressionStatement
import org.printscript.analyzer.id
import org.printscript.analyzer.number
import org.printscript.analyzer.rangeAt
import org.printscript.analyzer.string
import org.printscript.ast.AstNode
import org.printscript.ast.BinaryOperator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CallArgumentRuleTest {
    private val rule = printlnArgumentRule()

    private fun diagnosticsOf(node: AstNode): List<Diagnostic> {
        val found = mutableListOf<Diagnostic>()
        rule.check(node, DiagnosticEmitter { found.add(it) })
        return found
    }

    @Test
    fun `println con un identificador no reporta nada`() {
        assertTrue(diagnosticsOf(call("println", id("x"))).isEmpty())
    }

    @Test
    fun `println con un literal numerico no reporta nada`() {
        assertTrue(diagnosticsOf(call("println", number(1.0))).isEmpty())
    }

    @Test
    fun `println con un literal string no reporta nada`() {
        assertTrue(diagnosticsOf(call("println", string("hola"))).isEmpty())
    }

    @Test
    fun `println con una expresion binaria reporta un problema`() {
        val diagnostics = diagnosticsOf(call("println", binary(BinaryOperator.PLUS, number(1.0), number(2.0))))

        assertEquals(1, diagnostics.size)
        assertEquals("println-argument", diagnostics.single().rule)
        assertEquals(
            "println solo admite un identificador o un literal, no una expresión.",
            diagnostics.single().message,
        )
    }

    @Test
    fun `una llamada a una funcion que no es println no se revisa`() {
        val expression = binary(BinaryOperator.PLUS, number(1.0), number(2.0))

        assertTrue(diagnosticsOf(call("otraFuncion", expression)).isEmpty())
    }

    @Test
    fun `un ExpressionStatement no es una CallExpression y no se revisa`() {
        assertTrue(diagnosticsOf(expressionStatement(number(1.0))).isEmpty())
    }

    @Test
    fun `si hay varios argumentos invalidos reporta uno por cada uno`() {
        val first = binary(BinaryOperator.PLUS, number(1.0), number(2.0))
        val second = binary(BinaryOperator.TIMES, number(3.0), number(4.0))

        val diagnostics = diagnosticsOf(call("println", id("x"), first, second))

        assertEquals(2, diagnostics.size)
        assertEquals(setOf(first.range, second.range), diagnostics.map { it.range }.toSet())
    }

    @Test
    fun `reporta el range exacto del argumento invalido`() {
        val argumentRange = rangeAt(line = 2, column = 9, length = 5)
        val expression = binary(BinaryOperator.PLUS, number(1.0), number(2.0)).copy(range = argumentRange)

        val diagnostics = diagnosticsOf(call("println", expression))

        assertEquals(argumentRange, diagnostics.single().range)
    }

    // La misma clase con otro nombre de funcion: es toda la regla de 1.1.
    @Test
    fun `readInput con una expresion reporta un problema`() {
        val found = mutableListOf<Diagnostic>()
        val expression = binary(BinaryOperator.PLUS, string("Enter"), string("something"))

        readInputArgumentRule().check(call("readInput", expression), DiagnosticEmitter { found.add(it) })

        assertEquals(1, found.size)
        assertEquals("read-input-argument", found.single().rule)
    }

    @Test
    fun `readInput con un literal no reporta nada`() {
        val found = mutableListOf<Diagnostic>()

        readInputArgumentRule().check(call("readInput", string("Name:")), DiagnosticEmitter { found.add(it) })

        assertTrue(found.isEmpty())
    }

    // Cada fabrica mira solo su funcion: la de println no opina de un readInput.
    @Test
    fun `la regla de println no revisa un readInput`() {
        val expression = binary(BinaryOperator.PLUS, string("a"), string("b"))

        assertTrue(diagnosticsOf(call("readInput", expression)).isEmpty())
    }
}
