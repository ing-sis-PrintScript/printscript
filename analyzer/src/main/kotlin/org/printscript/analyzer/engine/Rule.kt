package org.printscript.analyzer.engine

import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.ast.ASTNode

internal interface Rule {
    fun check(
        node: ASTNode,
        emitter: DiagnosticEmitter,
    )
}
