package org.printscript.interpreter.io

interface PrintScriptIO {
    fun print(message: String)

    // El prompt llega para que quien implemente la interfaz pueda usarlo (el TCK
    // se lo pasa a su InputProvider), pero imprimirlo NO es tarea de acá: de eso
    // se encarga readInput. Si se imprimiera en los dos lados saldria dos veces.
    fun read(prompt: String): String

    // El valor de una variable de entorno, o null si no existe. Es una puerta mas
    // de la interfaz y no un System.getenv suelto adentro del interprete: asi un
    // test puede darle las variables que quiera sin tocar el entorno real.
    fun env(name: String): String?
}
