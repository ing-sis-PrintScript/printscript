package org.printscript.ast

import org.printscript.common.Range

sealed interface AstNode {
    val range: Range
}

sealed interface Statement : AstNode

sealed interface Expression : AstNode
