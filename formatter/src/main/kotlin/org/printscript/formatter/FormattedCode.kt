package org.printscript.formatter

data class FormattedCode(val text: String) {
    operator fun plus(other: FormattedCode): FormattedCode = FormattedCode(text + other.text)

    companion object {
        val EMPTY = FormattedCode("")
    }
}
