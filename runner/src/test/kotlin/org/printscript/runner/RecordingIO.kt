package org.printscript.runner

import org.printscript.interpreter.io.PrintScriptIO

/**
 * El io de los tests: guarda lo que se imprime, entrega las entradas que se le
 * cargaron y responde por las variables de entorno que se le den.
 *
 * Las variables vienen de un mapa y no de System.getenv justamente para esto:
 * un test puede fijar el entorno que necesita sin tocar el de verdad.
 */
class RecordingIO(
    inputs: List<String> = emptyList(),
    private val environment: Map<String, String> = emptyMap(),
) : PrintScriptIO {
    private val lines = mutableListOf<String>()
    private val pending = ArrayDeque(inputs)

    override fun print(message: String) {
        lines.add(message)
    }

    override fun read(prompt: String): String = pending.removeFirstOrNull() ?: ""

    override fun env(name: String): String? = environment[name]

    fun output(): List<String> = lines.toList()
}
