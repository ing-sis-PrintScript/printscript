package org.printscript.formatter.engine

import org.printscript.ast.ASTNode
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.FormatterContext

interface NodeFormatter<in T : ASTNode> {
    fun format(
        node: T,
        context: FormatterContext,
    ): FormattedCode
}

interface NodeDispatcher {
    fun formatOrNull(
        node: ASTNode,
        context: FormatterContext,
    ): FormattedCode?
}
