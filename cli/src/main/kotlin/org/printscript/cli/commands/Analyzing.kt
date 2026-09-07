package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.cli.progress.CountingProgress
import org.printscript.common.Result
import org.printscript.lexer.source.FileSourceReader
import org.printscript.runner.AnalyzeRunner
import org.printscript.runner.config.ConfigReadError
import org.printscript.runner.config.loadAnalyzerConfig

internal class Analyzing : CliktCommand(name = "analyzing") {
    private val source by argument(help = "Archivo PrintScript a analizar")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    private val config by option("--config", help = "Reglas de estilo en .yaml, .yml o .json")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    override fun run() =
        when (val loaded = analyzerConfig()) {
            is Result.Failure -> fail("config: ${loaded.error.message}")
            is Result.Success -> analyzeWith(loaded.value)
        }

    /** Sin --config valen los defaults, que no es un error sino el caso normal. */
    private fun analyzerConfig(): Result<AnalyzerConfig, ConfigReadError> {
        val file = config ?: return Result.Success(AnalyzerConfig())
        return loadAnalyzerConfig(file.name, file.readText())
    }

    /**
     * Los problemas se imprimen a medida que aparecen, no se juntan: el analyzer
     * los va emitiendo mientras recorre el archivo, y salen ya en orden.
     *
     * El contador es el unico estado, y es el minimo necesario para saber si hubo
     * problemas sin haberlos guardado.
     */
    private fun analyzeWith(config: AnalyzerConfig) {
        var problems = 0
        val progress = CountingProgress()

        AnalyzeRunner(config, progress).analyze(FileSourceReader.of(source)) { diagnostic ->
            problems++
            echo(line(diagnostic), err = true)
        }

        progress.done()

        if (problems == 0) {
            echo("✓ ${source.name} — sin problemas")
            return
        }

        fail("$problems ${if (problems == 1) "problema" else "problemas"}")
    }

    private fun line(diagnostic: Diagnostic): String =
        "${source.name}:${diagnostic.range.start}  " +
            "${diagnostic.severity.name.lowercase()}  ${diagnostic.rule}  ${diagnostic.message}"
}
