package org.printscript.formatter.config

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
