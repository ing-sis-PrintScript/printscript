package org.printscript.runner.config

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.printscript.common.Result

// Un solo mapper para los dos formatos: JSON es un subconjunto de YAML, asi que el
// parser de YAML lee los dos. No hay nada que detectar, y por eso esta clase no
// necesita saber como se llamaba el archivo: el TCK entrega un stream sin nombre.
internal class ConfigReader {
    fun readTree(text: String): Result<JsonNode, ConfigReadError> =
        try {
            val root = ObjectMapper(YAMLFactory()).readTree(text)
            when {
                root == null || root.isMissingNode || root.isNull -> Result.Success(EMPTY)
                root.isObject -> Result.Success(root)
                else -> Result.Failure(ConfigReadError("La configuracion tiene que ser un mapa de reglas"))
            }
        } catch (e: JacksonException) {
            Result.Failure(ConfigReadError("El archivo de configuracion no se pudo leer: ${e.message}"))
        }

    private companion object {
        private val EMPTY: JsonNode = JsonNodeFactory.instance.objectNode()
    }
}
