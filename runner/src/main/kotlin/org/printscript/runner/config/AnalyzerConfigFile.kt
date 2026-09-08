package org.printscript.runner.config

import com.fasterxml.jackson.databind.JsonNode
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.CamelCase
import org.printscript.analyzer.config.NamingConvention
import org.printscript.analyzer.config.SnakeCase
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map

// Los nombres los fija el TCK, no nosotros: son la especificacion. Ojo el guion bajo
// de identifier_format y el ESPACIO en los valores: "camel case", no "camelCase".
private const val NAMING = "identifier_format"
private const val PRINTLN_ARGUMENTS = "mandatory-variable-or-literal-in-println"
private const val READ_INPUT_ARGUMENTS = "mandatory-variable-or-literal-in-readInput"
private const val CAMEL = "camel case"
private const val SNAKE = "snake case"

fun loadAnalyzerConfig(text: String): Result<AnalyzerConfig, ConfigReadError> =
    ConfigReader().readTree(text).flatMap { root -> toAnalyzerConfig(root) }

private fun toAnalyzerConfig(root: JsonNode): Result<AnalyzerConfig, ConfigReadError> {
    val defaults: Result<AnalyzerConfig, ConfigReadError> = Result.Success(AnalyzerConfig())

    return root.properties().fold(defaults) { accumulated, (key, node) ->
        accumulated.flatMap { config -> applyRule(config, key, node) }
    }
}

private fun applyRule(
    config: AnalyzerConfig,
    key: String,
    node: JsonNode,
): Result<AnalyzerConfig, ConfigReadError> =
    when (key) {
        NAMING -> namingConventionOf(node).map { config.copy(namingConvention = it) }
        PRINTLN_ARGUMENTS -> booleanOf(key, node).map { config.copy(restrictPrintlnArguments = it) }
        READ_INPUT_ARGUMENTS -> booleanOf(key, node).map { config.copy(restrictReadInputArguments = it) }
        else -> Result.Failure(ConfigReadError("Regla de analisis desconocida: '$key'"))
    }

private fun namingConventionOf(node: JsonNode): Result<NamingConvention, ConfigReadError> =
    when {
        node.isTextual && node.textValue() == CAMEL -> Result.Success(CamelCase)
        node.isTextual && node.textValue() == SNAKE -> Result.Success(SnakeCase)
        else -> Result.Failure(ConfigReadError("'$NAMING' tiene que ser '$CAMEL' o '$SNAKE'"))
    }

private fun booleanOf(
    key: String,
    node: JsonNode,
): Result<Boolean, ConfigReadError> =
    if (node.isBoolean) {
        Result.Success(node.booleanValue())
    } else {
        Result.Failure(ConfigReadError("'$key' tiene que ser true o false"))
    }
