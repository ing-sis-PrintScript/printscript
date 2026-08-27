package org.printscript.formatter.config

sealed interface ConfigValue {
    data class BooleanValue(val value: Boolean) : ConfigValue

    data class IntValue(val value: Int) : ConfigValue
}
