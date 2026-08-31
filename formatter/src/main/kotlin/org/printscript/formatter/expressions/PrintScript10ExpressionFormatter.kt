package org.printscript.formatter.expressions

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.engine.NodeFormatter
import org.printscript.formatter.syntax.Syntax
import org.printscript.formatter.syntax.symbol
import java.math.BigDecimal

class PrintScript10ExpressionFormatter : NodeFormatter<Expression> {
    private val parenthesizer = Parenthesizer()

    override fun format(
        node: Expression,
        context: FormatterContext,
    ): FormattedCode =
        when (node) {
            is NumberLiteral -> FormattedCode(renderNumber(node.value))
            is StringLiteral -> FormattedCode(renderString(node.value))
            is Identifier -> FormattedCode(node.name)
            is BinaryExpression -> formatBinary(node, context)
            is CallExpression -> formatCall(node, context)
        }

    private fun formatBinary(
        node: BinaryExpression,
        context: FormatterContext,
    ): FormattedCode {
        val spacing = FormattedCode(Syntax.OPERATOR_SPACING.render())
        val operator = FormattedCode(node.operator.symbol())
        val left = formatOperand(node.operator, node.left, OperandSide.LEFT, context)
        val right = formatOperand(node.operator, node.right, OperandSide.RIGHT, context)
        return left + spacing + operator + spacing + right
    }

    private fun formatOperand(
        parent: BinaryOperator,
        operand: Expression,
        side: OperandSide,
        context: FormatterContext,
    ): FormattedCode {
        val formatted = format(operand, context)
        return when (parenthesizer.needsParentheses(parent, operand, side)) {
            true -> FormattedCode(Syntax.OPEN_PAREN) + formatted + FormattedCode(Syntax.CLOSE_PAREN)
            false -> formatted
        }
    }

    private fun formatCall(
        node: CallExpression,
        context: FormatterContext,
    ): FormattedCode {
        val arguments = node.arguments.map { format(it, context) }
        return format(node.callee, context) +
            FormattedCode(Syntax.OPEN_PAREN) +
            join(arguments, FormattedCode(Syntax.ARGUMENT_SEPARATOR)) +
            FormattedCode(Syntax.CLOSE_PAREN)
    }

    private fun join(
        parts: List<FormattedCode>,
        separator: FormattedCode,
    ): FormattedCode = parts.reduceOrNull { left, right -> left + separator + right } ?: FormattedCode.EMPTY
}

internal fun renderNumber(value: Double): String =
    when (value.isFinite()) {
        true -> BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
        false -> value.toString()
    }

internal fun renderString(value: String): String {
    val quote = quoteFor(value)
    return quote + value + quote
}

// Un contenido con los dos tipos de comilla no es representable en PrintScript 1.0.
private fun quoteFor(value: String): String =
    when {
        DOUBLE_QUOTE !in value -> DOUBLE_QUOTE
        SINGLE_QUOTE in value -> DOUBLE_QUOTE
        else -> SINGLE_QUOTE
    }

private const val DOUBLE_QUOTE = "\""
private const val SINGLE_QUOTE = "'"
