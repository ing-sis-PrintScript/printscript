package org.printscript.runner.config

import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.mapError
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterConfigLoader

fun loadFormatterConfig(text: String): Result<FormatterConfig, ConfigReadError> =
    ConfigReader().readTree(text)
        .flatMap { root -> toConfigValues(root) }
        .flatMap { values ->
            FormatterConfigLoader().load(values).mapError { ConfigReadError(it.message) }
        }
