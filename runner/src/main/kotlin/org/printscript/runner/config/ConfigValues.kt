package org.printscript.runner.config

import com.fasterxml.jackson.databind.JsonNode
import org.printscript.common.Result
import org.printscript.formatter.config.ConfigValue

internal fun toConfigValues(root: JsonNode): Result<Map<String, ConfigValue>, ConfigReadError> {
    val values = mutableMapOf<String, ConfigValue>()

    for ((key, node) in root.properties()) {
        val configValue =
            when {
                node.isBoolean -> ConfigValue.BooleanValue(node.booleanValue())
                node.isInt -> ConfigValue.IntValue(node.intValue())
                else -> return Result.Failure(
                    ConfigReadError("La regla '$key' tiene que ser un booleano o un numero entero"),
                )
            }
        values[key] = configValue
    }

    return Result.Success(values)
}
