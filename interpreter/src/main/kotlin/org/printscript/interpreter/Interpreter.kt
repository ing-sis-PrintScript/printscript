package org.printscript.interpreter

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO
import org.printscript.interpreter.statements.StatementExecutor

/**
 * Prueba los executors en orden hasta que uno reconoce el Statement, y
 * devuelve el Environment que dejó — sin guardar nada propio.
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
    private val executors: List<StatementExecutor> = PrintScript10.statementExecutors(),
) : PrintScriptInterpreter {
    override fun execute(
        statement: Statement,
        env: Environment,
    ): Result<Environment, InterpreterError> =
        executors.firstNotNullOfOrNull { it.execute(statement, env, io) }
            ?: Result.Failure(InterpreterError("No se sabe cómo ejecutar este statement.", statement.range))
}
