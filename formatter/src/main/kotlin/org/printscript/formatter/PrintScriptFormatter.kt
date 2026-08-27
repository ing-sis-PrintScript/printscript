package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.formatter.config.FormatterConfig

class PrintScriptFormatter(
    private val nodes: List<PartialNodeFormatter<ASTNode>>,
    private val separator: StatementSeparator,
    private val config: FormatterConfig,
) : Formatter {
    override fun format(
        program: Sequence<Result<ASTNode, PrintScriptError>>,
    ): Sequence<Result<FormattedCode, PrintScriptError>> {
        val context = FormatterContext(config)
        return program
            .mapIndexed { index, element -> element.flatMap { node -> emit(node, index == 0, context) } }
            .takeThrough { it is Result.Success }
    }

    private fun emit(
        node: ASTNode,
        isFirst: Boolean,
        context: FormatterContext,
    ): Result<FormattedCode, PrintScriptError> =
        when (val formatted = formatNode(node, context)) {
            null -> Result.Failure(UnsupportedNode(node))
            else -> Result.Success(separator.before(isFirst, node, context) + formatted + separator.after())
        }

    private fun formatNode(
        node: ASTNode,
        context: FormatterContext,
    ): FormattedCode? = nodes.firstNotNullOfOrNull { it.formatOrNull(node, context) }
}

private fun <T> Sequence<T>.takeThrough(predicate: (T) -> Boolean): Sequence<T> =
    sequence {
        for (element in this@takeThrough) {
            yield(element)
            if (!predicate(element)) return@sequence
        }
    }
