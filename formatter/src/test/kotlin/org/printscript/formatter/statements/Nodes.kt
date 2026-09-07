package org.printscript.formatter.statements

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.DeclaredType
import org.printscript.ast.Expression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.VariableDeclaration
import org.printscript.formatter.expressions.ANY_RANGE
import org.printscript.formatter.expressions.id

internal fun declaration(
    name: String,
    type: DeclaredType,
    initializer: Expression?,
) = VariableDeclaration(id(name), type, initializer, ANY_RANGE)

internal fun assignment(
    name: String,
    value: Expression,
) = AssignmentStatement(id(name), value, ANY_RANGE)

internal fun expressionStatement(expression: Expression) = ExpressionStatement(expression, ANY_RANGE)
