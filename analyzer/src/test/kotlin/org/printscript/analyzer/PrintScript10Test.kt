package org.printscript.analyzer

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.CamelCase
import org.printscript.analyzer.config.SnakeCase
import org.printscript.ast.BinaryOperator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScript10Test {
    @Test
    fun `con la configuracion por defecto, camelCase no se reporta y println invalido si`() {
        val analyzer = PrintScript10.analyzer(AnalyzerConfig())
        val statements =
            program(
                declaration("miVariable", initializer = number(1.0)),
                expressionStatement(call("println", binary(BinaryOperator.PLUS, number(1.0), number(2.0)))),
            )

        val diagnostics = analyzer.collectDiagnostics(statements)

        assertEquals(listOf("println-argument"), diagnostics.map { it.rule })
    }

    @Test
    fun `apagar restrictPrintlnArguments deja de reportar esa regla`() {
        val config = AnalyzerConfig(restrictPrintlnArguments = false)
        val analyzer = PrintScript10.analyzer(config)
        val statements =
            program(
                expressionStatement(call("println", binary(BinaryOperator.PLUS, number(1.0), number(2.0)))),
            )

        assertTrue(analyzer.collectDiagnostics(statements).isEmpty())
    }

    @Test
    fun `cambiar la convencion de camelCase a snake_case cambia que se reporta`() {
        val statements = program(declaration("mi_variable", initializer = number(1.0)))

        val withCamelCase = PrintScript10.analyzer(AnalyzerConfig(namingConvention = CamelCase))
        val withSnakeCase = PrintScript10.analyzer(AnalyzerConfig(namingConvention = SnakeCase))

        // El mismo identificador "mi_variable": molesta a camelCase, no a snake_case.
        assertEquals(listOf("identifier-naming"), withCamelCase.collectDiagnostics(statements).map { it.rule })
        assertTrue(withSnakeCase.collectDiagnostics(statements).isEmpty())
    }

    @Test
    fun `un programa sin problemas no reporta nada`() {
        val analyzer = PrintScript10.analyzer(AnalyzerConfig())
        val statements =
            program(
                declaration("total", initializer = number(0.0)),
                expressionStatement(call("println", id("total"))),
            )

        assertTrue(analyzer.collectDiagnostics(statements).isEmpty())
    }
}
