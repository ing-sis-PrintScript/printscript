package org.printscript.runner

import org.printscript.interpreter.io.PrintScriptIO

class RecordingIO : PrintScriptIO {
    private val lines = mutableListOf<String>()

    override fun print(message: String) {
        lines.add(message)
    }

    override fun read(prompt: String): String = ""

    fun output(): List<String> = lines.toList()
}
