package org.printscript.formatter

import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.token.TokenSource

interface Formatter {
    fun format(tokens: TokenSource): Sequence<Result<FormattedCode, PrintScriptError>>
}

data class FormattedCode(val text: String) {
    operator fun plus(other: FormattedCode): FormattedCode = FormattedCode(text + other.text)

    companion object {
        val EMPTY = FormattedCode("")
    }
}
