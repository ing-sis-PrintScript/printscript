package org.printscript.cli.commands

import org.printscript.common.Result
import org.printscript.runner.config.ConfigReadError
import java.io.File

private val EXTENSIONS = setOf("yaml", "yml", "json")

// El CLI si tiene el nombre del archivo, asi que valida la extension. Es una ayuda
// al usuario --avisarle temprano que --config apunta a cualquier cosa--, no una
// necesidad del parser: ConfigReader lee YAML y JSON con el mismo mapper.
internal fun configText(file: File): Result<String, ConfigReadError> =
    if (file.extension.lowercase() in EXTENSIONS) {
        Result.Success(file.readText())
    } else {
        Result.Failure(ConfigReadError("La configuracion tiene que ser .yaml, .yml o .json"))
    }
