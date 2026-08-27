package org.printscript.formatter.statements

import org.printscript.ast.Expression
import org.printscript.ast.VariableDeclaration
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.NodeFormatter
import org.printscript.formatter.Syntax

class DeclarationFormatter(
    private val expressions: NodeFormatter<Expression>,
) : NodeFormatter<VariableDeclaration> {
    override fun format(
        node: VariableDeclaration,
        context: FormatterContext,
    ): FormattedCode {
        val config = context.config
        return FormattedCode(Syntax.DECLARATION_KEYWORD) +
            FormattedCode(Syntax.KEYWORD_SPACING.render()) +
            FormattedCode(node.identifier.name) +
            FormattedCode(config.spaceBeforeColon.render()) +
            FormattedCode(Syntax.TYPE_SEPARATOR) +
            FormattedCode(config.spaceAfterColon.render()) +
            FormattedCode(node.declaredType.symbol()) +
            formatInitializer(node.initializer, context) +
            FormattedCode(Syntax.TERMINATOR)
    }

    private fun formatInitializer(
        initializer: Expression?,
        context: FormatterContext,
    ): FormattedCode =
        initializer?.let { expression ->
            val spacing = FormattedCode(context.config.spaceAroundAssignment.render())
            spacing + FormattedCode(Syntax.ASSIGN) + spacing + expressions.format(expression, context)
        } ?: FormattedCode.EMPTY
}
