package org.printscript.cli.commands

import org.printscript.common.Result
import org.printscript.runner.config.ConfigReadError
import java.io.File

private val EXTENSIONS = setOf("yaml", "yml", "json")

internal fun configText(file: File): Result<String, ConfigReadError> =
    if (file.extension.lowercase() in EXTENSIONS) {
        Result.Success(file.readText())
    } else {
        Result.Failure(ConfigReadError("La configuracion tiene que ser .yaml, .yml o .json"))
    }
