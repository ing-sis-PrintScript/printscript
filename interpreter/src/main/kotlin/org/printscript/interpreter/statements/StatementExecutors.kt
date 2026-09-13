package org.printscript.interpreter.statements

import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.io.PrintScriptIO

// El conjunto de executors de una version, y como se elige el que le
// corresponde a cada statement.
//
// Es un fun interface --y no una List<StatementExecutor> pelada-- por el mismo
// motivo que StatementParsers en el parser: el IfExecutor esta ADENTRO de la
// lista que el mismo necesita para ejecutar el cuerpo del if. Recibiendo "con
// que conseguir la lista" en vez de la lista, el ciclo se rompe.
fun interface StatementExecutors {
    fun all(): List<StatementExecutor>

    // Un solo lugar decide que executor agarra cada statement. Antes esto vivia
    // adentro de Interpreter; ahora lo usan Interpreter (los statements de
    // arriba de todo) y el IfExecutor (los de adentro del bloque).
    fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError> =
        all().firstNotNullOfOrNull { it.execute(statement, env, io) }
            ?: Result.Failure(InterpreterError("No se sabe cómo ejecutar este statement.", statement.range))
}
