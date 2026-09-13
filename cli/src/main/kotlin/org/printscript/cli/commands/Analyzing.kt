package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.cli.progress.CountingProgress
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.lexer.source.StreamSourceReader
import org.printscript.runner.AnalyzeRunner
import org.printscript.runner.config.ConfigReadError
import org.printscript.runner.config.loadAnalyzerConfig

internal class Analyzing : CliktCommand(name = "analyzing") {
    private val source by argument(help = "Archivo PrintScript a analizar")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    private val config by option("--config", help = "Reglas de estilo en .yaml, .yml o .json")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .required()

    private val version by versionOption()

    override fun run() =
        when (val loaded = analyzerConfig()) {
            is Result.Failure -> fail("config: ${loaded.error.message}")
            is Result.Success -> analyzeWith(loaded.value)
        }

    private fun analyzerConfig(): Result<AnalyzerConfig, ConfigReadError> =
        configText(config).flatMap { loadAnalyzerConfig(it) }

    private fun analyzeWith(config: AnalyzerConfig) {
        var problems = 0
        val progress = CountingProgress()

        AnalyzeRunner(config, version, progress).analyze({ StreamSourceReader.of(source) }) { diagnostic ->
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
