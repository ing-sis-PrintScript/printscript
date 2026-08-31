package org.printscript.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import org.printscript.common.Result
import org.printscript.lexer.source.StringSourceReader

fun main(args: Array<String>) = PrintScript().subcommands(Execution()).main(args)

class PrintScript : CliktCommand(name = "printscript") {
    override fun run() = Unit
}

class Execution : CliktCommand(name = "execution") {
    private val source by argument(help = "Archivo PrintScript a ejecutar")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val result = Runner().execute(StringSourceReader(source.readText()))
        if (result is Result.Failure) {
            echo("error: ${result.error.message}", err = true)
            echo("  ${source.name}:${result.error.range.start}", err = true)
            throw ProgramResult(1)
        }
    }
}
