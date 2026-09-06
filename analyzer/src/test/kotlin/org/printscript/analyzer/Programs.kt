package org.printscript.analyzer

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result

internal fun program(vararg nodes: ASTNode): Sequence<Result<ASTNode, PrintScriptError>> =
    nodes.asSequence().map { Result.Success(it) }

/** Un programa con un error de sintaxis intercalado, para probar que no corta el análisis. */
internal fun programWithSyntaxError(vararg nodes: ASTNode): Sequence<Result<ASTNode, PrintScriptError>> =
    sequenceOf(Result.Failure(FakeSyntaxError)) + program(*nodes)

private object FakeSyntaxError : PrintScriptError {
    override val message = "error de sintaxis simulado"
    override val range: Range = ANY_RANGE
}

/** Junta los Diagnostic emitidos por un Analyzer.analyze() en una lista, para poder assertear sobre ella. */
internal fun Analyzer.collectDiagnostics(program: Sequence<Result<ASTNode, PrintScriptError>>): List<Diagnostic> {
    val diagnostics = mutableListOf<Diagnostic>()
    analyze(program, DiagnosticEmitter { diagnostics.add(it) })
    return diagnostics
}
