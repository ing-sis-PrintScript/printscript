package org.printscript.lexer

import org.printscript.common.Version

// Que lexer corresponde a cada version del lenguaje.
//
// Vive aparte de Lexer.kt a proposito: la clase Lexer no sabe --ni tiene por que saber--
// que existen versiones. Recibe un TokenMatcher y tokeniza. Elegir cual armar es otra
// responsabilidad, y por eso es otro archivo.
//
// El when es exhaustivo: si manana aparece una 1.2, el compilador avisa aca.
fun lexerFor(version: Version): Lexer =
    when (version) {
        Version.V10 -> Lexer(TokenMatcher(PrintScript10.RULES))
        Version.V11 -> Lexer(TokenMatcher(PrintScript11.RULES))
    }
