package org.printscript.interpreter.statements

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.io.PrintScriptIO

/**
 * Ejecuta UN tipo de Statement.
 *
 * Espejo de StatementParser en el parser: canHandle elige, execute hace el
 * trabajo. Agregar un statement nuevo (ej IfStatement en 1.1) es agregar una
 * clase acá y registrarla en PrintScript10.statementExecutors() — ni
 * Interpreter ni los executors existentes se tocan. Antes de este cambio,
 * Interpreter.execute() era un único `when` sobre los tres statements: agregar
 * uno cuarto significaba editar ese `when`, lo opuesto a Open/Closed.
 *
 * Devuelve el Environment resultante en vez de mutar nada: mismo patrón que
 * Environment.declare/assign, que tampoco mutan y devuelven la versión nueva.
 */
interface StatementExecutor {
    fun canHandle(statement: Statement): Boolean

    fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError>
}
