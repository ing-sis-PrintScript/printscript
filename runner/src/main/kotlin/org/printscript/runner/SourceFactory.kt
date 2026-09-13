package org.printscript.runner

import org.printscript.lexer.source.SourceReader

fun interface SourceFactory {
    fun open(): SourceReader
}
