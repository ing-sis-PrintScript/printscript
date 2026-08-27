package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.ast.Expression
import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.common.map
import org.printscript.formatter.config.FormatterConfig

class PrintScriptFormatter(
    private val statements: NodeFormatter<Statement>,
    private val expressions: NodeFormatter<Expression>,
    private val separator: StatementSeparator,
    private val config: FormatterConfig,
) : Formatter {
    override fun format(
        program: Sequence<Result<ASTNode, PrintScriptError>>,
    ): Sequence<Result<FormattedCode, PrintScriptError>> {
        val context = FormatterContext(config)
        return program
            .takeThrough { it is Result.Success }
            .mapIndexed { index, element -> element.map { node -> emit(node, index == 0, context) } }
    }

    private fun emit(
        node: ASTNode,
        isFirst: Boolean,
        context: FormatterContext,
    ): FormattedCode = separator.before(isFirst, node, context) + formatNode(node, context) + separator.after()

    private fun formatNode(
        node: ASTNode,
        context: FormatterContext,
    ): FormattedCode =
        when (node) {
            is Statement -> statements.format(node, context)
            is Expression -> expressions.format(node, context)
        }
}

private fun <T> Sequence<T>.takeThrough(predicate: (T) -> Boolean): Sequence<T> =
    sequence {
        for (element in this@takeThrough) {
            yield(element)
            if (!predicate(element)) return@sequence
        }
    }
