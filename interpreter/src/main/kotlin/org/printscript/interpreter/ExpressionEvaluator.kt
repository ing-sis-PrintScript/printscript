package org.printscript.interpreter

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.BooleanLiteral
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.ast.UnaryExpression
import org.printscript.ast.UnaryOperator
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.interpreter.io.PrintScriptIO
import org.printscript.interpreter.statements.BuiltInFunction

/**
 * Evalúa una expresión y devuelve su valor.
 *
 * Recibe el io por parámetro, igual que StatementExecutor.execute: desde 1.1
 * una expresión puede leer y escribir (readInput imprime su prompt y lee una
 * línea), así que evaluar dejó de ser una operación puramente de lectura.
 */
class ExpressionEvaluator(
    private val builtIns: Map<String, BuiltInFunction> = PrintScript10.BUILT_INS,
) {
    fun evaluate(
        expression: Expression,
        env: Environment,
        io: PrintScriptIO,
    ): Result<PrintScriptValue, InterpreterError> {
        return when (expression) {
            is NumberLiteral -> Result.Success(PrintScriptValue.NumberValue(expression.value))
            is StringLiteral -> Result.Success(PrintScriptValue.StringValue(expression.value))
            is BooleanLiteral -> Result.Success(PrintScriptValue.BooleanValue(expression.value))
            is Identifier -> env.get(expression.name, expression.range)
            is BinaryExpression -> evaluateBinary(expression, env, io)
            is UnaryExpression -> evaluateUnary(expression, env, io)
            is CallExpression -> valueOf(expression, env, io)
        }
    }

    /**
     * Ejecuta una llamada. El valor es nullable porque hay funciones que no
     * devuelven nada.
     *
     * Es pública porque una llamada puede aparecer en dos lugares y el trabajo
     * es el mismo: adentro de una expresión, donde el valor hace falta, y sola
     * como statement, donde se descarta.
     */
    fun call(
        expression: CallExpression,
        env: Environment,
        io: PrintScriptIO,
    ): Result<PrintScriptValue?, InterpreterError> {
        val builtIn =
            builtIns[expression.callee.name]
                ?: return Result.Failure(
                    InterpreterError("No existe la función '${expression.callee.name}'.", expression.range),
                )

        // Los parsers de llamadas construyen SIEMPRE la llamada con exactamente
        // un argumento: la gramática no permite otra cosa. No es un chequeo
        // defensivo que falta — es una garantía del parser. Si algún día hay
        // funciones con otra cantidad de argumentos, hay que volver a validar acá.
        val argument = expression.arguments.first()

        return evaluate(argument, env, io).flatMap { value -> builtIn.call(value, expression.range, io) }
    }

    // Una llamada usada adentro de una expresión tiene que dejar un valor. Si la
    // función no devuelve nada, el programa está mal escrito y hay que decirlo.
    private fun valueOf(
        expression: CallExpression,
        env: Environment,
        io: PrintScriptIO,
    ): Result<PrintScriptValue, InterpreterError> =
        call(expression, env, io).flatMap { value ->
            value?.let { Result.Success(it) }
                ?: Result.Failure(
                    InterpreterError("'${expression.callee.name}' no devuelve un valor.", expression.range),
                )
        }

    private fun evaluateUnary(
        node: UnaryExpression,
        env: Environment,
        io: PrintScriptIO,
    ): Result<PrintScriptValue, InterpreterError> {
        val operandResult = evaluate(node.operand, env, io)
        if (operandResult is Result.Failure) return operandResult

        val operand = (operandResult as Result.Success).value
        if (operand !is PrintScriptValue.NumberValue) {
            return Result.Failure(InterpreterError("No se puede negar '$operand'.", node.range))
        }

        return when (node.operator) {
            UnaryOperator.MINUS -> Result.Success(PrintScriptValue.NumberValue(-operand.value))
        }
    }

    /**
     * Evalúa los dos lados y, si ambos salen bien, decide qué operación
     * corresponde. flatMap corta solo en el primer Failure — nada de
     * `if (... is Failure) return ...` a mano ni de castear el Success.
     */
    private fun evaluateBinary(
        node: BinaryExpression,
        env: Environment,
        io: PrintScriptIO,
    ): Result<PrintScriptValue, InterpreterError> =
        evaluate(node.left, env, io).flatMap { left ->
            evaluate(node.right, env, io).flatMap { right ->
                combine(node.operator, left, right, node.range)
            }
        }

    /** Decide QUÉ operación aplica según los tipos de los dos valores, ya evaluados. */
    private fun combine(
        operator: BinaryOperator,
        left: PrintScriptValue,
        right: PrintScriptValue,
        range: Range,
    ): Result<PrintScriptValue, InterpreterError> =
        when {
            isStringConcat(operator, left, right) ->
                Result.Success(PrintScriptValue.StringValue(left.toString() + right.toString()))

            left is PrintScriptValue.NumberValue && right is PrintScriptValue.NumberValue ->
                arithmetic(operator, left.value, right.value, range)

            else ->
                Result.Failure(InterpreterError("Operación inválida entre '$left' y '$right'.", range))
        }

    private fun isStringConcat(
        operator: BinaryOperator,
        left: PrintScriptValue,
        right: PrintScriptValue,
    ): Boolean =
        operator == BinaryOperator.PLUS &&
            (left is PrintScriptValue.StringValue || right is PrintScriptValue.StringValue)

    /** Las cuatro operaciones aritméticas, ya sabiendo que los dos lados son números. */
    private fun arithmetic(
        operator: BinaryOperator,
        left: Double,
        right: Double,
        range: Range,
    ): Result<PrintScriptValue, InterpreterError> =
        when (operator) {
            BinaryOperator.PLUS -> Result.Success(PrintScriptValue.NumberValue(left + right))
            BinaryOperator.MINUS -> Result.Success(PrintScriptValue.NumberValue(left - right))
            BinaryOperator.TIMES -> Result.Success(PrintScriptValue.NumberValue(left * right))
            BinaryOperator.DIVIDE -> divide(left, right, range)
        }

    private fun divide(
        left: Double,
        right: Double,
        range: Range,
    ): Result<PrintScriptValue, InterpreterError> =
        if (right == 0.0) {
            Result.Failure(InterpreterError("División por cero.", range))
        } else {
            Result.Success(PrintScriptValue.NumberValue(left / right))
        }
}
