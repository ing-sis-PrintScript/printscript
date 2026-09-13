package org.printscript.interpreter

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO
import org.printscript.interpreter.statements.StatementExecutors
import org.printscript.interpreter.versions.PrintScript10

/**
 * Ejecuta un Statement y devuelve el Environment que dejó — sin guardar nada
 * propio. Cuál executor le toca lo resuelve StatementExecutors, que es el mismo
 * despacho que usa el IfExecutor para el cuerpo del if.
 *
 * Es el mismo giro que ya tiene Parser respecto de TokenStream: el
 * coordinador no tiene estado, el estado es un valor que entra y sale
 * (Environment acá, TokenStream allá) y lo hila quien llama. Antes Interpreter
 * guardaba `private var env` y lo reasignaba en cada execute() — la única
 * mutación interna que le quedaba a los tres módulos con reglas de negocio
 * (lexer, parser, interpreter). Ahora no queda ninguna.
 */
class Interpreter(
    private val io: PrintScriptIO = StandardIO(),
    private val executors: StatementExecutors = PrintScript10.executors(),
) : PrintScriptInterpreter {
    override fun execute(
        statement: Statement,
        env: Environment,
    ): Result<Environment, InterpreterError> = executors.execute(statement, env, io)
}
