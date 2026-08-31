package org.printscript.interpreter

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.common.Result

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

    private fun evaluateBinary(
        node: BinaryExpression,
        env: Environment,
    ): Result<PrintScriptValue, InterpreterError> {
        val leftResult = evaluate(node.left, env)
        if (leftResult is Result.Failure) return leftResult

        val rightResult = evaluate(node.right, env)
        if (rightResult is Result.Failure) return rightResult

        val left = (leftResult as Result.Success).value
        val right = (rightResult as Result.Success).value

        if (node.operator == BinaryOperator.PLUS && (
                left is PrintScriptValue.StringValue ||
                    right is PrintScriptValue.StringValue
            )
        ) {
            return Result.Success(PrintScriptValue.StringValue(left.toString() + right.toString()))
        }

        if (left is PrintScriptValue.NumberValue && right is PrintScriptValue.NumberValue) {
            val l = left.value
            val r = right.value
            return when (node.operator) {
                BinaryOperator.PLUS -> Result.Success(PrintScriptValue.NumberValue(l + r))
                BinaryOperator.MINUS -> Result.Success(PrintScriptValue.NumberValue(l - r))
                BinaryOperator.TIMES -> Result.Success(PrintScriptValue.NumberValue(l * r))
                BinaryOperator.DIVIDE -> {
                    if (r == 0.0) {
                        Result.Failure(InterpreterError("División por cero.", node.range))
                    } else {
                        Result.Success(PrintScriptValue.NumberValue(l / r))
                    }
                }
            }
        }

        return Result.Failure(InterpreterError("Operación inválida entre '$left' y '$right'.", node.range))
    }
}
