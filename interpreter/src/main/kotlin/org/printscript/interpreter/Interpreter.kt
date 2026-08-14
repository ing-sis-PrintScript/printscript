package org.printscript.interpreter

import org.printscript.ast.*
import org.printscript.common.Result
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO

class Interpreter(
    private var env: Environment = Environment(),
    private val io: PrintScriptIO = StandardIO()
) : PrintScriptInterpreter {

    private val evaluator = ExpressionEvaluator()

    override fun execute(statement: Statement): Result<Unit, InterpreterError> {
        return when (statement) {
            is VariableDeclaration -> executeDeclaration(statement)
            is AssignmentStatement -> executeAssignment(statement)
            is ExpressionStatement -> executeExpressionStatement(statement)
        }
    }

    private fun executeDeclaration(node: VariableDeclaration): Result<Unit, InterpreterError> {
        var initialValue: PrintScriptValue? = null

        val initializerNode = node.initializer

        if (initializerNode != null) {
            val evalResult = evaluator.evaluate(initializerNode, env)
            if (evalResult is Result.Failure) return evalResult
            initialValue = (evalResult as Result.Success).value
        }

        val envResult = env.declare(node.identifier.name, node.declaredType, initialValue, node.range)

        return if (envResult is Result.Success) {
            env = envResult.value // Mutación controlada en caso de éxito
            Result.Success(Unit)
        } else {
            Result.Failure((envResult as Result.Failure).error)
        }
    }

    private fun executeAssignment(node: AssignmentStatement): Result<Unit, InterpreterError> {
        val evalResult = evaluator.evaluate(node.value, env)
        if (evalResult is Result.Failure) return evalResult

        val envResult = env.assign(node.target.name, (evalResult as Result.Success).value, node.range)

        return if (envResult is Result.Success) {
            env = envResult.value
            Result.Success(Unit)
        } else {
            Result.Failure((envResult as Result.Failure).error)
        }
    }

    private fun executeExpressionStatement(node: ExpressionStatement): Result<Unit, InterpreterError> {
        val expr = node.expression
        if (expr is CallExpression && expr.callee.name == "println") {
            val arg = expr.arguments.firstOrNull()
                ?: return Result.Failure(InterpreterError("println requiere al menos un argumento.", node.range))

            val evalResult = evaluator.evaluate(arg, env)
            if (evalResult is Result.Failure) return evalResult

            io.print((evalResult as Result.Success).value.toString())
            return Result.Success(Unit)
        } else {
            val evalResult = evaluator.evaluate(expr, env)
            if (evalResult is Result.Failure) return evalResult
            return Result.Success(Unit)
        }
    }
}