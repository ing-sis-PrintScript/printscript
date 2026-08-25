package org.printscript.interpreter.statements

import org.printscript.ast.Statement
import org.printscript.ast.VariableDeclaration
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.interpreter.Environment
import org.printscript.interpreter.ExpressionEvaluator
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.PrintScriptValue
import org.printscript.interpreter.io.PrintScriptIO

/**
 * declaration = "let", identifier, ":", type, [ "=", expression ], ";" ;
 *
 * Si hay inicializador lo evalúa; si no, declara la variable sin valor (queda
 * "no inicializada", y leerla antes de asignarle algo es error de Environment).
 */
class DeclarationExecutor(
    private val evaluator: ExpressionEvaluator = ExpressionEvaluator(),
) : StatementExecutor {
    override fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError>? {
        val declaration = statement as? VariableDeclaration ?: return null

        val initialValue: Result<PrintScriptValue?, InterpreterError> =
            declaration.initializer?.let { evaluator.evaluate(it, env) } ?: Result.Success(null)

        return initialValue.flatMap { value ->
            env.declare(declaration.identifier.name, declaration.declaredType, value, declaration.range)
        }
    }
}
