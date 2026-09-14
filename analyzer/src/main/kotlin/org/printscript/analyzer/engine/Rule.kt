package org.printscript.analyzer.engine

import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.ast.AstNode

internal interface Rule {
    fun check(
        node: AstNode,
        emitter: DiagnosticEmitter,
    )
}
