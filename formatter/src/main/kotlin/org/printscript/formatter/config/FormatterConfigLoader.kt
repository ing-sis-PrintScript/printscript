package org.printscript.formatter.config

import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map

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
            SPACE_BEFORE_COLON -> readSpacing(key, value).map { config.copy(spaceBeforeColon = it) }
            SPACE_AFTER_COLON -> readSpacing(key, value).map { config.copy(spaceAfterColon = it) }
            SPACE_AROUND_EQUALS -> readSpacing(key, value).map { config.copy(spaceAroundAssignment = it) }
            PRINTLN_LINE_BREAKS -> readBlankLines(key, value).map { config.copy(blankLinesBeforePrintln = it) }
            else -> Result.Failure(ConfigError.UnknownRule(key))
        }

    private fun readSpacing(
        key: String,
        value: ConfigValue,
    ): Result<Spacing, ConfigError> =
        when (value) {
            is ConfigValue.BooleanValue -> Result.Success(spacingOf(value.value))
            is ConfigValue.IntValue -> Result.Failure(ConfigError.WrongType(key, BOOLEAN))
        }

    private fun spacingOf(enforced: Boolean): Spacing = if (enforced) Spacing.SINGLE else Spacing.NONE

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
        const val PRINTLN_LINE_BREAKS = "line-breaks-after-println"

        const val BOOLEAN = "boolean"
        const val INT = "int"
    }
}
