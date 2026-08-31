package org.printscript.cli

import org.printscript.common.PrintScriptError
import org.printscript.common.Range

data class CliError(
    override val message: String,
    override val range: Range,
) : PrintScriptError
