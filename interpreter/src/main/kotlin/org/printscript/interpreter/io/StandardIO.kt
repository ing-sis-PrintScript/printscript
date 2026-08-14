package org.printscript.interpreter.io

class StandardIO : PrintScriptIO {
    override fun print(message: String) {
        println(message)
    }

    override fun read(prompt: String): String {
        println(prompt)
        return readlnOrNull() ?: ""
    }
}