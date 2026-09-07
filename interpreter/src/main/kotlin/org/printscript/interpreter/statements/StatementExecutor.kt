package org.printscript.interpreter.statements

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.io.PrintScriptIO

/**
 * Ejecuta UN tipo de Statement.
 *
 * Espejo de TokenRule en el lexer, no de StatementParser: un único método que
 * devuelve null cuando el statement no es el suyo, en vez de canHandle +
 * execute por separado. Con dos métodos, execute() necesitaba un `as` que solo
 * es seguro si quien llama respeta "primero canHandle, después execute" — una
 * garantía que el tipo no expresa. Acá no hace falta: cada executor hace su
 * propio `as?` y, si no matchea, devuelve null sin declarar nada más.
 *
 * Agregar un statement nuevo (ej IfStatement en 1.1) es agregar una clase acá
 * y registrarla en PrintScript10.statementExecutors() — ni Interpreter ni los
 * executors existentes se tocan. Antes de este cambio, Interpreter.execute()
 * era un único `when` sobre los tres statements: agregar uno cuarto
 * significaba editar ese `when`, lo opuesto a Open/Closed. La lista paga un
 * precio por esa libertad: si un statement nuevo no tiene executor
 * registrado, nadie lo nota hasta ejecutarlo (ver PrintScript10Test).
 *
 * Devuelve el Environment resultante en vez de mutar nada: mismo patrón que
 * Environment.declare/assign, que tampoco mutan y devuelven la versión nueva.
 */
interface StatementExecutor {
    fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError>?
}
