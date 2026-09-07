package org.printscript.cli.progress

import java.io.PrintStream

// Reescribe siempre el mismo renglon con cuantas sentencias van.
//
// Escribe a stderr porque el progreso es estado, no resultado: asi
// "formatting programa.ps > salida.ps" guarda el codigo formateado limpio y el
// progreso igual se ve en pantalla.
//
// El \r vuelve al principio del renglon. No es un codigo ANSI, es un caracter
// comun: no hace falta preguntarle a nadie si la consola lo soporta.
internal class CountingProgress(private val out: PrintStream = System.err) : Progress {
    // Dos variables, y las dos hacen falta: una cuenta, la otra recuerda cuanto
    // ocupaba el mensaje anterior para taparlo entero al reescribirlo.
    private var statements = 0
    private var width = 0

    override fun parsed() {
        statements++
        draw("Parseando... $statements sentencias")
    }

    // Borra el renglon antes de que el comando muestre el resultado.
    override fun done() = draw("")

    private fun draw(message: String) {
        out.print("\r" + message.padEnd(width))
        out.flush()
        width = message.length
    }
}
