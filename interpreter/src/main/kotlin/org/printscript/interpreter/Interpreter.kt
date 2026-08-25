package org.printscript.interpreter

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.common.map
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO
import org.printscript.interpreter.statements.StatementExecutor

/**
 * Coordina la ejecución: por cada Statement, elige quién lo sabe ejecutar y
 * aplica el Environment resultante.
 *
 * No sabe qué es una declaración ni cómo se imprime: eso lo saben los
 * StatementExecutor en statements/. Acá solo se elige uno por canHandle y se
 * lo deja actuar — el mismo patrón que ya usa Parser con StatementParser.
 */
class Interpreter(
    private var env: Environment = Environment(),
    private val io: PrintScriptIO = StandardIO(),
    private val executors: List<StatementExecutor> = PrintScript10.statementExecutors(),
) : PrintScriptInterpreter {
    override fun execute(statement: Statement): Result<Unit, InterpreterError> {
        val executor =
            executors.firstOrNull { it.canHandle(statement) }
                ?: return Result.Failure(
                    InterpreterError("No se sabe cómo ejecutar este statement.", statement.range),
                )

        return executor.execute(statement, env, io).map { newEnv -> env = newEnv }
    }
}
