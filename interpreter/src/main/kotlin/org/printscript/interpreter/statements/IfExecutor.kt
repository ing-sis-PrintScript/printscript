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

        if (condition !is PrintScriptValue.BooleanValue) {
            return Result.Failure(
                InterpreterError("La condición de un if tiene que ser un boolean.", ifStatement.condition.range),
            )
        }

        val branch = if (condition.value) ifStatement.thenBranch else ifStatement.elseBranch.orEmpty()
        return runBlock(branch, env, io)
    }

    private fun runBlock(
        branch: List<Statement>,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError> = runStatements(branch, env, io).map { inner -> inner.endScope(env) }

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
