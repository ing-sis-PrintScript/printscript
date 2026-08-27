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
import org.printscript.formatter.NodeFormatter
import org.printscript.formatter.Syntax

class PrintScript10ExpressionFormatter(
    private val parenthesizer: Parenthesizer = Parenthesizer(),
    private val numbers: NumberRenderer = NumberRenderer(),
    private val strings: StringRenderer = StringRenderer(),
) : NodeFormatter<Expression> {
    override fun format(
        node: Expression,
        context: FormatterContext,
    ): FormattedCode =
        when (node) {
            is NumberLiteral -> FormattedCode(numbers.render(node.value))
            is StringLiteral -> FormattedCode(strings.render(node.value))
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
