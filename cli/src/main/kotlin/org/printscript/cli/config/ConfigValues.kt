package org.printscript.cli.config

import com.fasterxml.jackson.databind.JsonNode
import org.printscript.common.Result
import org.printscript.formatter.config.ConfigValue

/**
 * Angosta lo que Jackson leyo a los dos tipos que las reglas aceptan, y rechaza
 * el resto con el nombre de la regla que esta mal.
 *
 * Recibe un JsonNode y no un Map<String, Any?>: JsonNode ya describe que puede
 * haber en un documento (booleano, entero, texto, lista, objeto). Any? seria mas
 * ancho que la realidad y obligaria a preguntar en runtime lo que aca ya se sabe.
 *
 * Que la clave exista y que el valor este en rango NO se verifica aca: de eso se
 * encarga FormatterConfigLoader, que sabe que reglas hay y que espera cada una.
 */
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
