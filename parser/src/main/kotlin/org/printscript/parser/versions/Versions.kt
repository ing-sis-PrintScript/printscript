package org.printscript.parser.versions

import org.printscript.common.Version
import org.printscript.parser.Parser

// Que parser corresponde a cada version del lenguaje. Ver el comentario de
// lexer/Versions.kt: la clase Parser no sabe que existen versiones.
fun parserFor(version: Version): Parser =
    when (version) {
        Version.V10 -> PrintScript10.parser()
        Version.V11 -> PrintScript11.parser()
    }
