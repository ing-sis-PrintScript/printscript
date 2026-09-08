package org.printscript.runner

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.Interpreter
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO
import org.printscript.runner.progress.Progress

class ExecuteRunner(
    io: PrintScriptIO = StandardIO(),
    private val progress: Progress = Progress.NONE,
) {
    private val interpreter = Interpreter(io)

    fun execute(source: SourceFactory): Result<Unit, PrintScriptError> {
        var environment = Environment()

        for (parsed in statements(source, progress)) {
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
