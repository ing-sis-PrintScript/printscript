package org.printscript.interpreter.statements

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.io.PrintScriptIO

interface StatementExecutor {
    fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError>?
}
