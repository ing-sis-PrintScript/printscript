package org.printscript.interpreter

import org.printscript.ast.*
import org.printscript.common.Result
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.io.StandardIO

class Interpreter(
    private val env: Environment = Environment(),
    private val io: PrintScriptIO = StandardIO()
) : PrintScriptInterpreter {

    private val evaluator = ExpressionEvaluator(env)

    override fun execute(statement: Statement): Result<Unit, InterpreterError> {
        return try {
            when (statement) {
                is VariableDeclaration -> executeDeclaration(statement)
                is AssignmentStatement -> executeAssignment(statement)
                is ExpressionStatement -> executeExpressionStatement(statement)
            }
            Result.Success(Unit)
        } catch (e: RuntimeError) {
            Result.Failure(InterpreterError(e.message ?: "Error desconocido", e.range))
        }
    }

    private fun executeDeclaration(node: VariableDeclaration) {
        val value = node.initializer?.let { evaluator.evaluate(it) }
        env.declare(node.identifier.name, node.declaredType, value, node.range)
    }

    private fun executeAssignment(node: AssignmentStatement) {
        val value = evaluator.evaluate(node.value)
        env.assign(node.target.name, value, node.range)
    }

    private fun executeExpressionStatement(node: ExpressionStatement) {
        val expr = node.expression
        if (expr is CallExpression && expr.callee.name == "println") {
            val arg = expr.arguments.firstOrNull()
                ?: throw RuntimeError("println requiere al menos un argumento.", node.range)

            val value = evaluator.evaluate(arg)
            io.print(evaluator.formatValue(value))
        } else {
            evaluator.evaluate(expr)
        }
    }
}