package org.printscript.interpreter

sealed interface PrintScriptValue {
    data class NumberValue(val value: Double) : PrintScriptValue {

        override fun toString(): String {
            return if (value % 1 == 0.0) {
                value.toLong().toString()
            } else {
                value.toString()
            }
        }
    }

    data class StringValue(val value: String) : PrintScriptValue {
        override fun toString() = value
    }
}