package org.printscript.cli.config

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.formatter.config.ConfigValue

internal class ConfigReader {
    fun read(
        fileName: String,
        text: String,
    ): Result<Map<String, ConfigValue>, ConfigReadError> =
        mapperFor(fileName)
            .flatMap { mapper -> parse(mapper, text) }
            .flatMap { root -> toConfigValues(root) }

    private fun mapperFor(fileName: String): Result<ObjectMapper, ConfigReadError> =
        when {
            fileName.endsWith(".yaml") || fileName.endsWith(".yml") -> Result.Success(ObjectMapper(YAMLFactory()))
            fileName.endsWith(".json") -> Result.Success(ObjectMapper())
            else -> Result.Failure(ConfigReadError("La configuracion tiene que ser .yaml, .yml o .json"))
        }

    /**
     * Unico try del repositorio. Jackson reporta sus fallas con excepciones; este
     * es el borde donde se traducen a Result, para que del try para adentro los
     * errores sigan siendo valores.
     *
     * readTree y no readValue: readValue exige que haya un documento y tira si el
     * archivo esta vacio o solo tiene comentarios. Un archivo asi no es un error,
     * es una config sin reglas, con todas en su default.
     */
    private fun parse(
        mapper: ObjectMapper,
        text: String,
    ): Result<JsonNode, ConfigReadError> =
        try {
            val root = mapper.readTree(text)
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
