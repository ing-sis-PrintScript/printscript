package org.printscript.ast

import org.printscript.common.Range

sealed interface ASTNode {
    val range: Range
}

sealed interface Statement : ASTNode

sealed interface Expression : ASTNode
