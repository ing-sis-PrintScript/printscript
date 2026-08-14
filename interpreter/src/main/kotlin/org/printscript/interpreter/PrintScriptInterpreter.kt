package org.printscript.interpreter
import org.printscript.ast.Statement
import org.printscript.common.Result

interface PrintScriptInterpreter {
    fun execute(statement: Statement): Result<Unit, InterpreterError>
}