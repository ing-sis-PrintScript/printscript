package org.printscript.formatter.expressions

import java.math.BigDecimal

class NumberRenderer {
    fun render(value: Double): String =
        when (value.isFinite()) {
            true -> BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
            false -> value.toString()
        }
}
