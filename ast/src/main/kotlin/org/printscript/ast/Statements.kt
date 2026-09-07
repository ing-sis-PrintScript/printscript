package org.printscript.ast

import org.printscript.common.Range

data class VariableDeclaration(
    val identifier: Identifier,
    val declaredType: DeclaredType,
    val initializer: Expression?,
    override val range: Range,
) : Statement

data class AssignmentStatement(
    val target: Identifier,
    val value: Expression,
    override val range: Range,
) : Statement

data class ExpressionStatement(
    val expression: Expression,
    override val range: Range,
) : Statement
