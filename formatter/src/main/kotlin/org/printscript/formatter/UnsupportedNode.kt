package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Range

data class UnsupportedNode(val node: ASTNode) : PrintScriptError {
    override val range: Range = node.range
    override val message: String = "Esta versión de PrintScript no formatea ${node::class.simpleName}"

    override fun toString() = "Error de formato en $range: $message"
}
