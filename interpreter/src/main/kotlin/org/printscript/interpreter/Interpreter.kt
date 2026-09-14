package org.printscript.interpreter

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO
import org.printscript.interpreter.statements.StatementExecutors
import org.printscript.interpreter.versions.PrintScript10

class Interpreter(
    private val io: PrintScriptIO = StandardIO(),
    private val executors: StatementExecutors = PrintScript10.executors(),
) : PrintScriptInterpreter {
    override fun execute(
        statement: Statement,
        env: Environment,
    ): Result<Environment, InterpreterError> = executors.execute(statement, env, io)
}
