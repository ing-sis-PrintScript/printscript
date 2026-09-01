package org.printscript.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import org.printscript.cli.commands.Execution
import org.printscript.cli.commands.Formatting
import org.printscript.cli.commands.Validation

fun main(args: Array<String>) = PrintScript().subcommands(Execution(), Validation(), Formatting()).main(args)

internal class PrintScript : CliktCommand(name = "printscript") {
    override fun run() = Unit
}
