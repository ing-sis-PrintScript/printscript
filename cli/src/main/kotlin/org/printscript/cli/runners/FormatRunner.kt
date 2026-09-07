package org.printscript.cli.runners

import org.printscript.cli.progress.Progress
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.PrintScript10
import org.printscript.formatter.config.FormatterConfig
import org.printscript.lexer.source.SourceReader

internal class FormatRunner(
    private val config: FormatterConfig,
    private val progress: Progress = Progress.NONE,
) {
    fun format(source: SourceReader): Sequence<Result<FormattedCode, PrintScriptError>> =
        PrintScript10.formatter(config).format(statements(source, progress))
}
