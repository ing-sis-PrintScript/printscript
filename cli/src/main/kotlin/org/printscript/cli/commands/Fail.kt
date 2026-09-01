package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult

/**
 * Unico throw del repositorio, y el unico lugar donde el programa decide terminar
 * mal.
 *
 * ProgramResult no transporta un error: los errores del usuario ya se imprimieron
 * como valores antes de llegar aca. Es el mecanismo de Clikt para fijar el codigo
 * de salida --su propio main() lo atrapa dos frames mas arriba-- y una excepcion
 * es la unica forma que da la JVM de terminar con codigo distinto de cero.
 *
 * Devuelve Nothing: el compilador sabe que despues de un fail() no sigue nada.
 */
internal fun CliktCommand.fail(message: String): Nothing {
    echo(message, err = true)
    throw ProgramResult(1)
}
