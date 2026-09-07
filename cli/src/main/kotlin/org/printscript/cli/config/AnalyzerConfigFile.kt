package org.printscript.cli.config

import com.fasterxml.jackson.databind.JsonNode
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.CamelCase
import org.printscript.analyzer.config.NamingConvention
import org.printscript.analyzer.config.SnakeCase
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map

private const val NAMING = "identifier-naming"
private const val PRINTLN_ARGUMENTS = "println-only-simple-arguments"
private const val CAMEL = "camel-case"
private const val SNAKE = "snake-case"

internal fun loadAnalyzerConfig(
    fileName: String,
    text: String,
): Result<AnalyzerConfig, ConfigReadError> =
    ConfigReader().readTree(fileName, text).flatMap { root -> toAnalyzerConfig(root) }

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
