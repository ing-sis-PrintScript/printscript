package org.printscript.analyzer

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.CallExpression
import org.printscript.ast.DeclaredType
import org.printscript.ast.Expression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.ast.VariableDeclaration
import org.printscript.common.Position
import org.printscript.common.Range

internal val ANY_RANGE = Range(Position(1, 1), Position(1, 1))

internal fun rangeAt(
    line: Int,
    column: Int,
    length: Int = 1,
) = Range(Position(line, column), Position(line, column + length - 1))

internal fun number(
    value: Double,
    range: Range = ANY_RANGE,
) = NumberLiteral(value, range)

internal fun string(
    value: String,
    range: Range = ANY_RANGE,
) = StringLiteral(value, range)

internal fun id(
    name: String,
    range: Range = ANY_RANGE,
) = Identifier(name, range)

internal fun binary(
    operator: BinaryOperator,
    left: Expression,
    right: Expression,
) = BinaryExpression(operator, left, right, ANY_RANGE)

internal fun call(
    callee: String,
    vararg arguments: Expression,
) = CallExpression(id(callee), arguments.toList(), ANY_RANGE)

internal fun declaration(
    name: String,
    type: DeclaredType = DeclaredType.NUMBER,
    initializer: Expression? = null,
    identifierRange: Range = ANY_RANGE,
) = VariableDeclaration(id(name, identifierRange), type, initializer, ANY_RANGE)

internal fun assignment(
    name: String,
    value: Expression,
) = AssignmentStatement(id(name), value, ANY_RANGE)

internal fun expressionStatement(expression: Expression) = ExpressionStatement(expression, ANY_RANGE)
