package org.printscript.lexer.source

import java.util.concurrent.atomic.AtomicInteger

class ContadorDeLineas {
    private val leidas = AtomicInteger(0)

    fun registrarLectura() {
        leidas.incrementAndGet()
    }

    fun total(): Int = leidas.get()
}

class FuenteInfinita(
    private val linea: String,
    private val contador: ContadorDeLineas,
) : SourceReader {
    override fun nextLine(): LineReadResult {
        contador.registrarLectura()
        return LineReadResult.Success(linea, this)
    }
}
