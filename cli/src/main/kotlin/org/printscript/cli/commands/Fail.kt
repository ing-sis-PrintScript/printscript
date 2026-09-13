package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult

internal fun CliktCommand.fail(message: String): Nothing {
    echo(message, err = true)
    throw ProgramResult(1)
}
