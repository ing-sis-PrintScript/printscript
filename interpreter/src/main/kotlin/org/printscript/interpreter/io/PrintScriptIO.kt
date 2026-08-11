package org.printscript.interpreter.io

interface PrintScriptIO {
    fun print(message: String)
    fun read(prompt: String): String
}