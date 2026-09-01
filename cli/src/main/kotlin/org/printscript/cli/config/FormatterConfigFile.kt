package org.printscript.cli.config

import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.mapError
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterConfigLoader

/**
 * Las dos mitades de leer la config del formatter:
 *   ConfigReader          archivo -> Map<String, ConfigValue>   (leer y tipar)
 *   FormatterConfigLoader Map     -> FormatterConfig            (validar reglas)
 *
 * La segunda ya existia en el modulo formatter, que a proposito no toca el
 * filesystem. mapError unifica los dos tipos de error en uno solo.
 */
internal fun loadFormatterConfig(
    fileName: String,
    text: String,
): Result<FormatterConfig, ConfigReadError> =
    ConfigReader().read(fileName, text)
        .flatMap { values ->
            FormatterConfigLoader().load(values).mapError { ConfigReadError(it.message) }
        }
