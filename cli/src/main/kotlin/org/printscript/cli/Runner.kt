package org.printscript.cli

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.Interpreter
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO
import org.printscript.lexer.Lexer
import org.printscript.lexer.source.SourceReader
import org.printscript.parser.PrintScript10

class Runner(io: PrintScriptIO = StandardIO()) {
    private val interpreter = Interpreter(io)

    fun execute(source: SourceReader): Result<Unit, PrintScriptError> {
        var environment = Environment()

        for (parsed in PrintScript10.parser().parse(Lexer().tokenize(source))) {
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
