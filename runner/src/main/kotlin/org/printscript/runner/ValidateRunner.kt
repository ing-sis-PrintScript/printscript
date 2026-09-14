package org.printscript.runner

import org.printscript.common.PrintScriptError
import org.printscript.common.Version
import org.printscript.common.errorOrNull
import org.printscript.runner.progress.Progress

class ValidateRunner(
    private val version: Version,
    private val progress: Progress = Progress.NONE,
) {
    fun validate(source: SourceFactory): List<PrintScriptError> =
        statements(source, version, progress).mapNotNull { it.errorOrNull() }.toList()
}
