package org.printscript.analyzer

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.CamelCase
import org.printscript.analyzer.config.SnakeCase
import org.printscript.ast.BinaryOperator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScript10Test {
    // Los defaults son apagado: un config vacio no arma ninguna regla.
    @Test
    fun `con la configuracion por defecto no se reporta nada`() {
        val analyzer = PrintScript10.analyzer(AnalyzerConfig())
        val statements =
            program(
                declaration("mi_variable", initializer = number(1.0)),
                expressionStatement(call("println", binary(BinaryOperator.PLUS, number(1.0), number(2.0)))),
            )

        assertTrue(analyzer.collectDiagnostics(statements).isEmpty())
    }

    @Test
    fun `prender restrictPrintlnArguments reporta el println con expresion`() {
        val analyzer = PrintScript10.analyzer(AnalyzerConfig(restrictPrintlnArguments = true))
        val statements =
            program(
                expressionStatement(call("println", binary(BinaryOperator.PLUS, number(1.0), number(2.0)))),
            )

        assertEquals(listOf("println-argument"), analyzer.collectDiagnostics(statements).map { it.rule })
    }

    @Test
    fun `apagar restrictPrintlnArguments deja de reportar esa regla`() {
        val config = AnalyzerConfig(namingConvention = CamelCase, restrictPrintlnArguments = false)
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
        val analyzer = PrintScript10.analyzer(AnalyzerConfig(CamelCase, restrictPrintlnArguments = true))
        val statements =
            program(
                declaration("total", initializer = number(0.0)),
                expressionStatement(call("println", id("total"))),
            )

        assertTrue(analyzer.collectDiagnostics(statements).isEmpty())
    }
}
