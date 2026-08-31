package org.printscript.formatter

import org.printscript.ast.ASTNode

fun interface PartialNodeFormatter<in T : ASTNode> {
    fun formatOrNull(
        node: T,
        context: FormatterContext,
    ): FormattedCode?
}
