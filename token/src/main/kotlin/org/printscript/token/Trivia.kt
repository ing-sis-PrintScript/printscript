package org.printscript.token

// El whitespace que precedia a un token en el fuente: espacios, tabs y los
// saltos de linea que se cruzaron desde el token anterior.
//
// El TEXTO es la verdad, todo lo demas se deriva de el. Guardar contadores en
// su lugar perderia informacion: "\n    " (salto y despues indentacion) y
// "    \n" (basura al final de la linea) tienen los mismos contadores y
// significan cosas distintas.
//
// El formatter incremental lo necesita para devolver cada token con el espacio
// que traia cuando ninguna regla le compete.
data class Trivia(val text: String) {
    val isEmpty: Boolean get() = text.isEmpty()

    // Cuantos saltos separan este token del anterior. 0 es "misma linea".
    val lineBreaks: Int get() = text.count { it == '\n' }

    // Lo que hay despues del ultimo salto: la indentacion de esta linea.
    val indentation: String get() = text.substringAfterLast('\n', "")

    companion object {
        val EMPTY = Trivia("")
    }
}
