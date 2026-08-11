package org.printscript.ast

import org.printscript.common.Range

data class NumberLiteral(
    val value: Double,
    override val range: Range,
) : Expression


data class StringLiteral(
    val value: String,
    override val range: Range,
) : Expression


data class Identifier(
    val name: String,
    override val range: Range,
) : Expression


data class BinaryExpression(
    val operator: BinaryOperator,
    val left: Expression,
    val right: Expression,
    override val range: Range,
) : Expression


data class CallExpression(
    val callee: Identifier,
    val arguments: List<Expression>,
    override val range: Range,
) : Expression