package org.printscript.interpreter.versions

import org.printscript.common.Version
import org.printscript.interpreter.Interpreter
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO

fun interpreterFor(
    version: Version,
    io: PrintScriptIO = StandardIO(),
): Interpreter =
    when (version) {
        Version.V10 -> Interpreter(io, PrintScript10.executors())
        Version.V11 -> Interpreter(io, PrintScript11.executors())
    }
