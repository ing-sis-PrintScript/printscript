package org.printscript.interpreter.io

class StandardIO : PrintScriptIO {
    override fun print(message: String) {
        println(message)
    }

    override fun read(prompt: String): String = readlnOrNull() ?: ""

    override fun env(name: String): String? = System.getenv(name)
}
