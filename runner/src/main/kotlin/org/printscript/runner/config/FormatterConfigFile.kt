package org.printscript.runner.config

import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.mapError
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterConfigLoader

// Las tres mitades de leer la config del formatter:
//   ConfigReader          archivo -> JsonNode                   (leer)
//   toConfigValues        JsonNode -> Map<String, ConfigValue>  (tipar)
//   FormatterConfigLoader Map     -> FormatterConfig            (validar reglas)
//
// La ultima ya existia en el modulo formatter, que a proposito no toca el
// filesystem. mapError unifica los dos tipos de error en uno solo.
fun loadFormatterConfig(
    fileName: String,
    text: String,
): Result<FormatterConfig, ConfigReadError> =
    ConfigReader().readTree(fileName, text)
        .flatMap { root -> toConfigValues(root) }
        .flatMap { values ->
            FormatterConfigLoader().load(values).mapError { ConfigReadError(it.message) }
        }
