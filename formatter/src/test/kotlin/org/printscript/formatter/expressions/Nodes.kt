package org.printscript.formatter.expressions

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.common.Position
import org.printscript.common.Range

internal val ANY_RANGE = Range(Position(1, 1), Position(1, 1))

internal fun number(value: Double) = NumberLiteral(value, ANY_RANGE)

internal fun string(value: String) = StringLiteral(value, ANY_RANGE)

internal fun id(name: String) = Identifier(name, ANY_RANGE)

internal fun binary(
    operator: BinaryOperator,
    left: Expression,
    right: Expression,
) = BinaryExpression(operator, left, right, ANY_RANGE)

internal fun call(
    callee: String,
    vararg arguments: Expression,
) = CallExpression(id(callee), arguments.toList(), ANY_RANGE)
