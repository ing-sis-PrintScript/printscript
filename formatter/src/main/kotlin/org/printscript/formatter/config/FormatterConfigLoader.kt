package org.printscript.formatter.config

import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map

sealed interface ConfigValue {
    data class BooleanValue(val value: Boolean) : ConfigValue

    data class IntValue(val value: Int) : ConfigValue
}

sealed interface ConfigError {
    val message: String

    data class UnknownRule(val key: String) : ConfigError {
        override val message: String = "Unknown formatting rule: '$key'"
    }

    data class WrongType(
        val key: String,
        val expected: String,
    ) : ConfigError {
        override val message: String = "Rule '$key' expects a $expected value"
    }

    data class OutOfRange(
        val key: String,
        val value: Int,
        val allowed: IntRange,
    ) : ConfigError {
        override val message: String = "Rule '$key' expects a value within $allowed, got $value"
    }
}

class FormatterConfigLoader {
    fun load(values: Map<String, ConfigValue>): Result<FormatterConfig, ConfigError> {
        val defaults: Result<FormatterConfig, ConfigError> = Result.Success(FormatterConfig())
        return values.entries.fold(defaults) { accumulated, (key, value) ->
            accumulated.flatMap { config -> applyRule(config, key, value) }
        }
    }

    private fun applyRule(
        config: FormatterConfig,
        key: String,
        value: ConfigValue,
    ): Result<FormatterConfig, ConfigError> =
        when (key) {
            SPACE_BEFORE_COLON ->
                readSpacing(key, value, Spacing.SINGLE, Spacing.NONE).map { config.copy(spaceBeforeColon = it) }
            SPACE_AFTER_COLON ->
                readSpacing(key, value, Spacing.SINGLE, Spacing.NONE).map { config.copy(spaceAfterColon = it) }
            SPACE_AROUND_EQUALS ->
                readSpacing(key, value, Spacing.SINGLE, Spacing.NONE).map { config.copy(spaceAroundAssignment = it) }
            // La clave inversa gobierna el mismo campo, solo que al reves.
            NO_SPACE_AROUND_EQUALS ->
                readSpacing(key, value, Spacing.NONE, Spacing.SINGLE).map { config.copy(spaceAroundAssignment = it) }
            PRINTLN_LINE_BREAKS -> readBlankLines(key, value).map { config.copy(lineBreaksAfterPrintln = it) }
            LINE_BREAK_AFTER_STATEMENT ->
                readBoolean(key, value).map { config.copy(lineBreakAfterStatement = it) }
            SPACE_SURROUNDING_OPERATIONS ->
                readBoolean(key, value).map { config.copy(spaceSurroundingOperations = it) }
            SINGLE_SPACE_SEPARATION ->
                readBoolean(key, value).map { config.copy(singleSpaceSeparation = it) }
            else -> Result.Failure(ConfigError.UnknownRule(key))
        }

    // onTrue y onFalse los pone quien llama porque hay claves que dicen lo contrario
    // entre si: enforce-spacing-around-equals y enforce-no-spacing-around-equals.
    private fun readSpacing(
        key: String,
        value: ConfigValue,
        onTrue: Spacing,
        onFalse: Spacing,
    ): Result<Spacing, ConfigError> = readBoolean(key, value).map { if (it) onTrue else onFalse }

    private fun readBoolean(
        key: String,
        value: ConfigValue,
    ): Result<Boolean, ConfigError> =
        when (value) {
            is ConfigValue.BooleanValue -> Result.Success(value.value)
            is ConfigValue.IntValue -> Result.Failure(ConfigError.WrongType(key, BOOLEAN))
        }

    private fun readBlankLines(
        key: String,
        value: ConfigValue,
    ): Result<BlankLines, ConfigError> =
        when (value) {
            is ConfigValue.IntValue -> blankLinesOf(key, value.value)
            is ConfigValue.BooleanValue -> Result.Failure(ConfigError.WrongType(key, INT))
        }

    private fun blankLinesOf(
        key: String,
        count: Int,
    ): Result<BlankLines, ConfigError> =
        when (val blankLines = BlankLines.of(count)) {
            null -> Result.Failure(ConfigError.OutOfRange(key, count, BlankLines.ALLOWED))
            else -> Result.Success(blankLines)
        }

    private companion object {
        const val SPACE_BEFORE_COLON = "enforce-spacing-before-colon-in-declaration"
        const val SPACE_AFTER_COLON = "enforce-spacing-after-colon-in-declaration"
        const val SPACE_AROUND_EQUALS = "enforce-spacing-around-equals"
        const val NO_SPACE_AROUND_EQUALS = "enforce-no-spacing-around-equals"
        const val PRINTLN_LINE_BREAKS = "line-breaks-after-println"
        const val LINE_BREAK_AFTER_STATEMENT = "mandatory-line-break-after-statement"
        const val SPACE_SURROUNDING_OPERATIONS = "mandatory-space-surrounding-operations"
        const val SINGLE_SPACE_SEPARATION = "mandatory-single-space-separation"

        const val BOOLEAN = "boolean"
        const val INT = "int"
    }
}
