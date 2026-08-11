package org.printscript.interpreter

import org.printscript.ast.*

class ExpressionEvaluator(private val env: Environment) {

    fun evaluate(expression: Expression): Any {
        return when (expression) {
            is NumberLiteral -> expression.value
            is StringLiteral -> expression.value
            is Identifier -> env.get(expression.name, expression.range)
            is BinaryExpression -> evaluateBinary(expression)
            is CallExpression -> throw RuntimeError("Solo se soporta la llamada a 'println'.", expression.range)
        }
    }

    private fun evaluateBinary(node: BinaryExpression): Any {
        val left = evaluate(node.left)
        val right = evaluate(node.right)

        if (node.operator == BinaryOperator.PLUS && (left is String || right is String)) {
            return formatValue(left) + formatValue(right)
        }

        if (left is Number && right is Number) {
            val l = left.toDouble()
            val r = right.toDouble()
            return when (node.operator) {
                BinaryOperator.PLUS -> l + r
                BinaryOperator.MINUS -> l - r
                BinaryOperator.TIMES -> l * r
                BinaryOperator.DIVIDE -> {
                    if (r == 0.0) throw RuntimeError("División por cero.", node.range)
                    l / r
                }
            }
        }

        throw RuntimeError("Operación inválida entre '$left' y '$right'.", node.range)
    }

    fun formatValue(value: Any): String {
        return if (value is Double && value % 1 == 0.0) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}