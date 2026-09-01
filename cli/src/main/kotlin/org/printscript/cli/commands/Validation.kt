package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
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
        fail("${errors.size} ${if (errors.size == 1) "error" else "errores"}")
    }
}
