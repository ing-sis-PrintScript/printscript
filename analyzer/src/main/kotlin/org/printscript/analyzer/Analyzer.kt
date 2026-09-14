package org.printscript.analyzer

import org.printscript.ast.AstNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result

interface Analyzer {
    fun analyze(
        program: Sequence<Result<AstNode, PrintScriptError>>,
        emit: DiagnosticEmitter,
    )
}
