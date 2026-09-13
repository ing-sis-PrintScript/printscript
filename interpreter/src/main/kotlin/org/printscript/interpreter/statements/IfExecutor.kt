package org.printscript.interpreter.statements

import org.printscript.ast.IfStatement
import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.common.map
import org.printscript.interpreter.Environment
import org.printscript.interpreter.ExpressionEvaluator
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.PrintScriptValue
import org.printscript.interpreter.io.PrintScriptIO

/**
 * if = "if", "(", expression, ")", "{", { statement }, "}", [ "else", "{", { statement }, "}" ] ;
 *
 * Evalúa la condición, elige una rama y ejecuta sus statements en orden. La
 * rama que no se elige no se ejecuta: es el primer executor para el que una
 * parte del programa parseado puede no correr nunca.
 */
class IfExecutor(
    private val evaluator: ExpressionEvaluator = ExpressionEvaluator(),
    private val executors: StatementExecutors,
) : StatementExecutor {
    override fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError>? {
        val ifStatement = statement as? IfStatement ?: return null

        val conditionResult = evaluator.evaluate(ifStatement.condition, env, io)
        if (conditionResult is Result.Failure) return conditionResult
        val condition = (conditionResult as Result.Success).value

        // El parser acepta cualquier expresion como condicion: que sea un
        // boolean se sabe recien acá, con el valor en la mano.
        if (condition !is PrintScriptValue.BooleanValue) {
            return Result.Failure(
                InterpreterError("La condición de un if tiene que ser un boolean.", ifStatement.condition.range),
            )
        }

        // Sin else, la rama falsa es no hacer nada.
        val branch = if (condition.value) ifStatement.thenBranch else ifStatement.elseBranch.orEmpty()
        return runBlock(branch, env, io)
    }

    private fun runBlock(
        branch: List<Statement>,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError> = runStatements(branch, env, io).map { inner -> inner.endScope(env) }

    // tailrec y no un fold: cada statement necesita el Environment que dejo el
    // anterior, y el primero que falla corta la cadena.
    private tailrec fun runStatements(
        pending: List<Statement>,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError> {
        val statement = pending.firstOrNull() ?: return Result.Success(env)

        return when (val result = executors.execute(statement, env, io)) {
            is Result.Failure -> result
            is Result.Success -> runStatements(pending.drop(1), result.value, io)
        }
    }
}
