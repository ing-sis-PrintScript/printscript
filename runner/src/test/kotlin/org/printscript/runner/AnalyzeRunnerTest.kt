package org.printscript.runner

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.Severity
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.CamelCase
import org.printscript.analyzer.config.SnakeCase
import org.printscript.lexer.source.StringSourceReader
import org.printscript.runner.progress.Progress
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalyzeRunnerTest {
    private val findings = mutableListOf<Diagnostic>()

    // Los defaults del config son apagado, asi que los tests que quieren ver reportes
    // piden explicitamente las dos reglas prendidas.
    private val todasPrendidas = AnalyzerConfig(CamelCase, restrictPrintlnArguments = true)

    private fun analyze(
        source: String,
        config: AnalyzerConfig = todasPrendidas,
    ) {
        AnalyzeRunner(config).analyze({ StringSourceReader(source) }) { findings.add(it) }
    }

    @Test
    fun `un archivo prolijo no reporta nada`() {
        analyze("let miVariable: number = 5;\nprintln(miVariable);")

        assertEquals(emptyList(), findings)
    }

    // El caso valid-no-rules del TCK: sin reglas configuradas no se reporta nada,
    // aunque el archivo mezcle convenciones.
    @Test
    fun `sin reglas configuradas no reporta nada`() {
        analyze("let miVariable: number = 5;\nlet mi_variable: number = 10;", AnalyzerConfig())

        assertEquals(emptyList(), findings)
    }

    @Test
    fun `reporta el identificador que no sigue la convencion`() {
        analyze("let mi_variable: number = 5;")

        assertEquals("identifier-naming", findings.single().rule)
        assertEquals(1, findings.single().range.start.line)
    }

    @Test
    fun `la convencion sale de la config`() {
        analyze("let mi_variable: number = 5;", AnalyzerConfig(namingConvention = SnakeCase))

        assertEquals(emptyList(), findings)
    }

    @Test
    fun `reporta println con una expresion como argumento`() {
        analyze("println(1 + 2);")

        assertEquals("println-argument", findings.single().rule)
    }

    @Test
    fun `apagar la regla de println la apaga`() {
        analyze("println(1 + 2);", AnalyzerConfig(restrictPrintlnArguments = false))

        assertEquals(emptyList(), findings)
    }

    /**
     * El analyzer descarta los errores de sintaxis; el runner los reporta igual,
     * por el mismo emitter. Sin esto, analyzing sobre un archivo roto no diria una
     * palabra del error.
     */
    @Test
    fun `un error de sintaxis se reporta como un problema mas`() {
        analyze("let x: number = ;")

        assertEquals("syntax", findings.single().rule)
        assertEquals(Severity.ERROR, findings.single().severity)
    }

    @Test
    fun `sigue analizando despues de un error de sintaxis, y en orden`() {
        analyze(
            """
            let x: number = ;
            let mi_variable: number = 3;
            """.trimIndent(),
        )

        assertEquals(listOf("syntax", "identifier-naming"), findings.map { it.rule })
        assertEquals(listOf(1, 2), findings.map { it.range.start.line })
    }

    @Test
    fun `avisa una vez por cada sentencia parseada`() {
        var notices = 0

        AnalyzeRunner(AnalyzerConfig(), Progress { notices++ })
            .analyze({ StringSourceReader("let x: number = 1;\nlet y: number = 2;") }) { }

        assertEquals(2, notices)
    }
}
