package org.printscript.cli.config

/**
 * Un problema al leer el archivo de configuracion. No implementa PrintScriptError
 * porque no tiene posicion en el codigo fuente: no es un error del programa del
 * usuario, es un error del archivo de config.
 */
internal data class ConfigReadError(val message: String)
