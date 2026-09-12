package org.printscript.interpreter

import org.printscript.common.Version
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO

// Que interpreter corresponde a cada version del lenguaje.
//
// Lo que cambia entre versiones es la lista de executors, no la clase que los despacha:
// Interpreter recibe la lista ya armada y no sabe de donde salio.
fun interpreterFor(
    version: Version,
    io: PrintScriptIO = StandardIO(),
): Interpreter =
    when (version) {
        Version.V10 -> Interpreter(io, PrintScript10.statementExecutors())
        Version.V11 -> Interpreter(io, PrintScript11.statementExecutors())
    }
