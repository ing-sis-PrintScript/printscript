package org.printscript.interpreter

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
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

class ExpressionEvaluator {
    fun evaluate(
        expression: Expression,
        env: Environment,
    ): Result<PrintScriptValue, InterpreterError> {
        return when (expression) {
            is NumberLiteral -> Result.Success(PrintScriptValue.NumberValue(expression.value))
            is StringLiteral -> Result.Success(PrintScriptValue.StringValue(expression.value))
            is Identifier -> env.get(expression.name, expression.range)
            is BinaryExpression -> evaluateBinary(expression, env)
            is UnaryExpression -> evaluateUnary(expression, env)
            // Las llamadas conocidas (println) se despachan desde
            // ExpressionStatementExecutor antes de llegar acá. Si una
            // CallExpression sí llega a evaluate(), es porque apareció
            // anidada dentro de otra expresión — algo que la gramática de
            // 1.0 no produce, pero que 1.1 podría habilitar (ej "1 + f()").
            is CallExpression ->
                Result.Failure(
                    InterpreterError(
                        "No se puede usar la llamada a '${expression.callee.name}' dentro de una expresión.",
                        expression.range,
                    ),
                )
        }
    }

    private fun evaluateUnary(
        node: UnaryExpression,
        env: Environment,
    ): Result<PrintScriptValue, InterpreterError> {
        val operandResult = evaluate(node.operand, env)
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
    ): Result<PrintScriptValue, InterpreterError> =
        evaluate(node.left, env).flatMap { left ->
            evaluate(node.right, env).flatMap { right ->
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
