package org.printscript.runner

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.interpreter.Environment
import org.printscript.interpreter.interpreterFor
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO
import org.printscript.runner.progress.Progress

class ExecuteRunner(
    private val version: Version,
    io: PrintScriptIO = StandardIO(),
    private val progress: Progress = Progress.NONE,
) {
    private val interpreter = interpreterFor(version, io)

    fun execute(source: SourceFactory): Result<Unit, PrintScriptError> {
        var environment = Environment()

        for (parsed in statements(source, version, progress)) {
            val statement =
                when (parsed) {
                    is Result.Failure -> return parsed
                    is Result.Success -> parsed.value
                }
            when (val executed = interpreter.execute(statement, environment)) {
                is Result.Failure -> return executed
                is Result.Success -> environment = executed.value
            }
        }

        return Result.Success(Unit)
    }
}
