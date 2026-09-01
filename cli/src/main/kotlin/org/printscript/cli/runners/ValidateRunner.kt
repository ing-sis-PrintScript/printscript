package org.printscript.cli.runners

import org.printscript.common.PrintScriptError
import org.printscript.common.errorOrNull
import org.printscript.lexer.source.SourceReader

internal class ValidateRunner {
    fun validate(source: SourceReader): List<PrintScriptError> =
        statements(source).mapNotNull { it.errorOrNull() }.toList()
}
