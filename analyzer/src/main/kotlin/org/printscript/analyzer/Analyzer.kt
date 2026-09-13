package org.printscript.analyzer

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result

interface Analyzer {
    fun analyze(
        program: Sequence<Result<ASTNode, PrintScriptError>>,
        emit: DiagnosticEmitter,
    )
}
