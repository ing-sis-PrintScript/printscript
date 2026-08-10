package org.printscript.interpreter

interface PrintScriptIO {
    fun print(message: String)
    fun read(prompt: String): String
}