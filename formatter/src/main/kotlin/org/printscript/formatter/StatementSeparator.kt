package org.printscript.formatter

import org.printscript.ast.ASTNode

class StatementSeparator(private val println: PrintlnRecognizer = PrintlnRecognizer()) {
    fun before(
        isFirst: Boolean,
        node: ASTNode,
        context: FormatterContext,
    ): FormattedCode =
        when {
            isFirst -> FormattedCode.EMPTY
            println.matches(node) -> FormattedCode(context.config.blankLinesBeforePrintln.render())
            else -> FormattedCode.EMPTY
        }

    fun after(): FormattedCode = FormattedCode(Syntax.STATEMENT_SEPARATOR)
}
