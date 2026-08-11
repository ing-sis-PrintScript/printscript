package org.printscript.common

interface PrintScriptError {
    val message: String
    val range: Range
}