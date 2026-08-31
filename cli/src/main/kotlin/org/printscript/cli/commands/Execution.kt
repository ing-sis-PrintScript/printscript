package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import org.printscript.cli.runners.ExecuteRunner
import org.printscript.common.Result
import org.printscript.lexer.source.StringSourceReader

internal class Execution : CliktCommand(name = "execution") {
    private val source by argument(help = "Archivo PrintScript a ejecutar")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val result = ExecuteRunner().execute(StringSourceReader(source.readText()))

        if (result is Result.Failure) {
            echo("${source.name}:${result.error.range.start}  ${result.error.message}", err = true)
            // ProgramResult no transporta un error: los errores ya se imprimieron arriba
            // como valores. Es el mecanismo de Clikt para fijar el codigo de salida y su
            // propio main() lo atrapa dos frames mas arriba. Es la unica forma que da la
            // JVM de terminar con codigo distinto de cero.
            throw ProgramResult(1)
        }
    }
}
