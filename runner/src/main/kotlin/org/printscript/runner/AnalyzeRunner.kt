package org.printscript.runner

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.PrintScript10
import org.printscript.analyzer.Severity
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.runner.progress.Progress

private const val SYNTAX = "syntax"

class AnalyzeRunner(
    private val config: AnalyzerConfig,
    private val progress: Progress = Progress.NONE,
) {
    // Por cada sentencia: si fallo reporto el error, si salio bien la analizo.
    // Todo sale por el mismo emitter, en el orden en que aparece en el archivo.
    fun analyze(
        source: SourceFactory,
        emit: DiagnosticEmitter,
    ) {
        val analyzer = PrintScript10.analyzer(config)

        for (step in statements(source, progress)) {
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
