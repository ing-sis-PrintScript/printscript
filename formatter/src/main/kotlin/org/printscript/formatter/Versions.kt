package org.printscript.formatter

import org.printscript.common.Version
import org.printscript.formatter.config.FormatterConfig

// Que formatter corresponde a cada version del lenguaje. Igual que en el lexer, el
// parser y el interpreter: la clase que formatea no sabe que existen versiones, recibe
// las reglas ya armadas.
fun formatterFor(
    version: Version,
    config: FormatterConfig,
): Formatter =
    when (version) {
        Version.V10 -> PrintScript10.formatter(config)
        Version.V11 -> PrintScript11.formatter(config)
    }
