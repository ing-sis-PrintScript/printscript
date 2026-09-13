package org.printscript.interpreter.statements

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.interpreter.Environment
import org.printscript.interpreter.ExpressionEvaluator
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.io.PrintScriptIO

class AssignmentExecutor(
    private val evaluator: ExpressionEvaluator = ExpressionEvaluator(),
) : StatementExecutor {
    override fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError>? {
        val assignment = statement as? AssignmentStatement ?: return null

        return evaluator.evaluate(assignment.value, env, io).flatMap { value ->
            env.assign(assignment.target.name, value, assignment.range)
        }
    }
}
