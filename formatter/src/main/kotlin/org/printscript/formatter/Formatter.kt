package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result

interface Formatter {
    fun format(program: Sequence<Result<ASTNode, PrintScriptError>>): Sequence<Result<FormattedCode, PrintScriptError>>
}
