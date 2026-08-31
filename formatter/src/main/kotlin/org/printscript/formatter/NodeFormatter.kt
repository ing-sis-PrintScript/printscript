package org.printscript.formatter

import org.printscript.ast.ASTNode

fun interface NodeFormatter<in T : ASTNode> {
    fun format(
        node: T,
        context: FormatterContext,
    ): FormattedCode
}
