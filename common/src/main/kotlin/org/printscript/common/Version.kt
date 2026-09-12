package org.printscript.common

// Que version del lenguaje se esta procesando.
//
// Vive en common porque la necesitan el lexer, el parser, el interpreter y el runner
// para elegir con que reglas trabajar. Antes existia solo adentro del adaptador del TCK,
// asi que el repo principal no sabia hablar de versiones y todo el pipeline asumia 1.0.
enum class Version(val id: String) {
    V10("1.0"),
    V11("1.1"),
    ;

    companion object {
        // Null y no excepcion: una version desconocida es un dato invalido que llega de
        // afuera --del CLI o del TCK--, no un bug. Quien llama decide como reportarlo.
        fun of(id: String): Version? = entries.firstOrNull { it.id == id }
    }
}
