package org.printscript.interpreter.statements

import org.printscript.ast.CallExpression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.common.map
import org.printscript.interpreter.Environment
import org.printscript.interpreter.ExpressionEvaluator
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.io.PrintScriptIO

/**
 * Ejecuta una expresión usada como statement: sola en su línea, sin que nadie
 * use su valor.
 *
 * Las dos ramas evalúan lo mismo y descartan el resultado; la diferencia es que
 * una llamada acá puede no devolver nada (println), y evaluate() exige un
 * valor. Por eso call() y no evaluate() cuando es una llamada.
 */
class ExpressionStatementExecutor(
    private val evaluator: ExpressionEvaluator = ExpressionEvaluator(),
) : StatementExecutor {
    override fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError>? {
        val expression = (statement as? ExpressionStatement)?.expression ?: return null

        return if (expression is CallExpression) {
            evaluator.call(expression, env, io).map { env }
        } else {
            evaluator.evaluate(expression, env, io).map { env }
        }
    }
}
