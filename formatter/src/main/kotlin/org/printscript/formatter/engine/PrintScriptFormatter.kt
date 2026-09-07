package org.printscript.formatter.engine

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.Formatter
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.syntax.StatementSeparator

class PrintScriptFormatter(
    private val dispatcher: NodeDispatcher,
    private val separator: StatementSeparator,
    private val config: FormatterConfig,
) : Formatter {
    override fun format(
        program: Sequence<Result<ASTNode, PrintScriptError>>,
    ): Sequence<Result<FormattedCode, PrintScriptError>> =
        sequence {
            val context = FormatterContext(config)
            val elements = program.iterator()
            var isFirst = true
            var failed = false
            while (!failed && elements.hasNext()) {
                val result = emit(elements.next(), isFirst, context)
                yield(result)
                failed = result is Result.Failure
                isFirst = false
            }
        }

    private fun emit(
        element: Result<ASTNode, PrintScriptError>,
        isFirst: Boolean,
        context: FormatterContext,
    ): Result<FormattedCode, PrintScriptError> =
        when (element) {
            is Result.Failure -> element
            is Result.Success -> formatNode(element.value, isFirst, context)
        }

    private fun formatNode(
        node: ASTNode,
        isFirst: Boolean,
        context: FormatterContext,
    ): Result<FormattedCode, PrintScriptError> =
        when (val formatted = dispatcher.formatOrNull(node, context)) {
            null -> Result.Failure(UnsupportedNode(node))
            else -> Result.Success(separator.before(isFirst, node, context) + formatted + separator.after())
        }
}
