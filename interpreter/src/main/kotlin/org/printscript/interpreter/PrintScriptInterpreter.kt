package org.printscript.interpreter

import org.printscript.ast.Statement
import org.printscript.common.Result

/**
 * Ejecuta UN Statement contra UN Environment y devuelve el Environment
 * resultante — nunca mantiene estado propio entre llamadas. Quien llama es
 * quien hila el Environment de un execute() al siguiente, igual que Parser
 * hila TokenStream de un paso al próximo sin guardarlo él mismo.
 */
interface PrintScriptInterpreter {
    fun execute(
        statement: Statement,
        env: Environment,
    ): Result<Environment, InterpreterError>
}
