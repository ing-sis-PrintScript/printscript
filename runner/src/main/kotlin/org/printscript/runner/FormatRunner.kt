package org.printscript.runner

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.formatter.FormattedCode
import org.printscript.formatter.PrintScript10
import org.printscript.formatter.config.FormatterConfig
import org.printscript.lexer.lexerFor

// Unico comando que NO parsea: formatear es preservar el espaciado del fuente, y el AST
// no lo tiene. Por eso tampoco avisa progreso: no hay sentencias que contar.
class FormatRunner(
    private val config: FormatterConfig,
    private val version: Version,
) {
    fun format(source: SourceFactory): Sequence<Result<FormattedCode, PrintScriptError>> =
        PrintScript10.formatter(config).format(lexerFor(version).tokenize(source.open()))
}
