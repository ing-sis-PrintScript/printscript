package org.printscript.cli.progress

import org.printscript.runner.progress.Progress
import java.io.PrintStream

internal class CountingProgress(private val out: PrintStream = System.err) : Progress {
    private var statements = 0
    private var width = 0

    override fun parsed() {
        statements++
        draw("Parseando... $statements sentencias")
    }

    override fun done() {
        if (statements == 0) return

        out.println()
        out.flush()
    }

    private fun draw(message: String) {
        out.print("\r" + message.padEnd(width))
        out.flush()
        width = message.length
    }
}
