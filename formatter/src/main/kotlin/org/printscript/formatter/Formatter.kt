package org.printscript.formatter

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.formatter.config.FormatterConfig

interface Formatter {
    fun format(program: Sequence<Result<ASTNode, PrintScriptError>>): Sequence<Result<FormattedCode, PrintScriptError>>
}

data class FormattedCode(val text: String) {
    operator fun plus(other: FormattedCode): FormattedCode = FormattedCode(text + other.text)

    companion object {
        val EMPTY = FormattedCode("")
    }
}

data class FormatterContext(val config: FormatterConfig)
