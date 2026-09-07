package org.printscript.interpreter.statements

import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Statement
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map
import org.printscript.interpreter.Environment
import org.printscript.interpreter.ExpressionEvaluator
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.io.PrintScriptIO

/**
 * Ejecuta una expresión usada como statement: sola en su línea, sin que nadie
 * use su valor.
 *
 * Si es una llamada a una función conocida (hoy solo println), la despacha
 * por el registro de BuiltInFunctions. Si no es una llamada, simplemente
 * evalúa la expresión y descarta el resultado — válido pero sin efecto
 * (ej "2 + 2;").
 */
class ExpressionStatementExecutor(
    private val evaluator: ExpressionEvaluator = ExpressionEvaluator(),
    private val builtIns: Map<String, BuiltInFunction> = BuiltInFunctions.REGISTRY,
) : StatementExecutor {
    override fun execute(
        statement: Statement,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError>? {
        val expression = (statement as? ExpressionStatement)?.expression ?: return null

        return if (expression is CallExpression) {
            executeCall(expression, env, io)
        } else {
            evaluateAndDiscard(expression, env)
        }
    }

    private fun executeCall(
        call: CallExpression,
        env: Environment,
        io: PrintScriptIO,
    ): Result<Environment, InterpreterError> {
        val builtIn =
            builtIns[call.callee.name]
                ?: return Result.Failure(InterpreterError("No existe la función '${call.callee.name}'.", call.range))

        // CallParser (parser/statements/CallParser.kt) construye SIEMPRE la
        // llamada con exactamente un argumento: la gramática de 1.0 no permite
        // otra cosa. Esto no es un chequeo defensivo "por las dudas" — es una
        // garantía del parser. El código viejo pedía firstOrNull() y devolvía
        // un InterpreterError si no había argumentos, un camino que ningún
        // programa real podía disparar. Si algún día hay funciones variádicas,
        // esta garantía deja de valer y hay que volver a validar el tamaño acá.
        // Mientras tanto, si esta garantía se rompiera igual (un CallExpression
        // armado a mano fuera del parser, por ejemplo), el síntoma no sería un
        // Result.Failure como en el resto del sistema: sería una
        // NoSuchElementException sin capturar — la única excepción de todo
        // el pipeline, que no respeta la convención de Result de common.
        val argument = call.arguments.first()

        return evaluator.evaluate(argument, env).flatMap { value ->
            builtIn.call(value, io).map { env }
        }
    }

    private fun evaluateAndDiscard(
        expression: Expression,
        env: Environment,
    ): Result<Environment, InterpreterError> = evaluator.evaluate(expression, env).map { env }
}
