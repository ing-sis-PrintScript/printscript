package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import org.printscript.cli.runners.ValidateRunner
import org.printscript.lexer.source.StringSourceReader

internal class Validation : CliktCommand(name = "validation") {
    private val source by argument(help = "Archivo PrintScript a validar")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val errors = ValidateRunner().validate(StringSourceReader(source.readText()))

        if (errors.isEmpty()) {
            echo("✓ ${source.name} — sin errores")
            return
        }

        errors.forEach { echo("${source.name}:${it.range.start}  ${it.message}", err = true) }
        echo("${errors.size} ${if (errors.size == 1) "error" else "errores"}", err = true)
        // ProgramResult no transporta un error: los errores ya se imprimieron arriba
        // como valores. Es el mecanismo de Clikt para fijar el codigo de salida y su
        // propio main() lo atrapa dos frames mas arriba. Es la unica forma que da la
        // JVM de terminar con codigo distinto de cero.
        throw ProgramResult(1)
    }
}
