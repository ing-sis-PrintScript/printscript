package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import org.printscript.cli.progress.CountingProgress
import org.printscript.cli.runners.ExecuteRunner
import org.printscript.common.Result
import org.printscript.lexer.source.FileSourceReader

internal class Execution : CliktCommand(name = "execution") {
    private val source by argument(help = "Archivo PrintScript a ejecutar")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val progress = CountingProgress()
        val result = ExecuteRunner(progress = progress).execute(FileSourceReader.of(source))
        progress.done()

        if (result is Result.Failure) {
            fail("${source.name}:${result.error.range.start}  ${result.error.message}")
        }
    }
}
