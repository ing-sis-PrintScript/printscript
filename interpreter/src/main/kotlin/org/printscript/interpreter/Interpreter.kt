package org.printscript.interpreter

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.common.map
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO
import org.printscript.interpreter.statements.StatementExecutor

/**
 * Coordina la ejecución: por cada Statement, prueba los executors en orden
 * hasta que uno lo reconoce, y aplica el Environment resultante.
 *
 * No sabe qué es una declaración ni cómo se imprime: eso lo saben los
 * StatementExecutor en statements/. Como cada executor devuelve null cuando el
 * statement no es el suyo (ver StatementExecutor), firstNotNullOfOrNull prueba
 * uno por uno y se queda con el primero que sí contesta — nadie castea nada
 * acá, y el que sí matchea es quien decide con un `as?` propio.
 */
class Interpreter(
    private var env: Environment = Environment(),
    private val io: PrintScriptIO = StandardIO(),
    private val executors: List<StatementExecutor> = PrintScript10.statementExecutors(),
) : PrintScriptInterpreter {
    override fun execute(statement: Statement): Result<Unit, InterpreterError> {
        val result =
            executors.firstNotNullOfOrNull { it.execute(statement, env, io) }
                ?: return Result.Failure(
                    InterpreterError("No se sabe cómo ejecutar este statement.", statement.range),
                )

        return result.map { newEnv -> env = newEnv }
    }
}
