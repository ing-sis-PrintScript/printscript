package org.printscript.cli.progress

// Aviso de que se parseo una sentencia mas.
// No lleva numero: quien quiera contar, cuenta, y asi el pipeline no necesita variables.
// Tampoco hay porcentaje: saber el total exige leer el archivo entero, que es lo que evitamos.
internal fun interface Progress {
    fun parsed()

    // Al terminar, cada uno limpia lo suyo. El que no muestra nada no tiene nada que limpiar.
    fun done() {}

    companion object {
        // Para los tests y para quien no quiera mostrar nada.
        val NONE = Progress { }
    }
}
