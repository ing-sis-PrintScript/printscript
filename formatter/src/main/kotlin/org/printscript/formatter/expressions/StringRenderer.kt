package org.printscript.formatter.expressions

class StringRenderer {
    fun render(value: String): String {
        val quote = quoteFor(value)
        return quote + value + quote
    }

    // Un contenido con los dos tipos de comilla no es representable en PrintScript 1.0.
    private fun quoteFor(value: String): String =
        when {
            DOUBLE !in value -> DOUBLE
            SINGLE in value -> DOUBLE
            else -> SINGLE
        }

    private companion object {
        const val DOUBLE = "\""
        const val SINGLE = "'"
    }
}
