package org.printscript.cli.runners

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.PrintScript10
import org.printscript.analyzer.Severity
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.lexer.source.SourceReader

private const val SYNTAX = "syntax"

internal class AnalyzeRunner(private val config: AnalyzerConfig) {
    /**
     * Por cada sentencia: si fallo reporto el error, si salio bien la analizo.
     * Todo sale por el mismo emitter, en el orden en que aparece en el archivo.
     *
     * El analyzer descarta los Result.Failure a proposito --un error de sintaxis
     * no es un problema de estilo-- pero para quien corre el comando los dos son
     * lo mismo: un problema, en una posicion, con una severidad. Por eso un error
     * de sintaxis se reporta tambien como Diagnostic, con rule "syntax".
     *
     * Se llama al analyzer con una sentencia por vez: PrintScriptAnalyzer no
     * guarda nada entre elementos, asi que el resultado es identico, y de esta
     * forma los dos casos quedan uno al lado del otro.
     */
    fun analyze(
        source: SourceReader,
        emit: DiagnosticEmitter,
    ) {
        val analyzer = PrintScript10.analyzer(config)

        for (step in statements(source)) {
            when (step) {
                is Result.Failure -> emit.emit(syntaxProblem(step.error))
                is Result.Success -> analyzer.analyze(sequenceOf(step), emit)
            }
        }
    }

    private fun syntaxProblem(error: PrintScriptError) =
        Diagnostic(
            rule = SYNTAX,
            message = error.message,
            range = error.range,
            severity = Severity.ERROR,
        )
}
