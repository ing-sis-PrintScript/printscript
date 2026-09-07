package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import org.printscript.cli.progress.CountingProgress
import org.printscript.common.Result
import org.printscript.formatter.config.FormatterConfig
import org.printscript.lexer.source.FileSourceReader
import org.printscript.runner.FormatRunner
import org.printscript.runner.config.ConfigReadError
import org.printscript.runner.config.loadFormatterConfig

internal class Formatting : CliktCommand(name = "formatting") {
    private val source by argument(help = "Archivo PrintScript a formatear")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    private val config by option("--config", help = "Reglas de formato en .yaml, .yml o .json")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    override fun run() =
        when (val loaded = formatterConfig()) {
            is Result.Failure -> fail("config: ${loaded.error.message}")
            is Result.Success -> formatWith(loaded.value)
        }

    /** Sin --config valen los defaults, que no es un error sino el caso normal. */
    private fun formatterConfig(): Result<FormatterConfig, ConfigReadError> {
        val file = config ?: return Result.Success(FormatterConfig())
        return loadFormatterConfig(file.name, file.readText())
    }

    private fun formatWith(config: FormatterConfig) {
        val progress = CountingProgress()

        for (result in FormatRunner(config, progress).format(FileSourceReader.of(source))) {
            when (result) {
                // Cada trozo ya trae sus separadores: se imprime crudo, sin agregar saltos.
                is Result.Success -> echo(result.value.text, trailingNewline = false)
                is Result.Failure -> {
                    progress.done()
                    fail("${source.name}:${result.error.range.start}  ${result.error.message}")
                }
            }
        }

        progress.done()
    }
}
