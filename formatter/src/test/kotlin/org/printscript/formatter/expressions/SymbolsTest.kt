package org.printscript.formatter.expressions

import org.printscript.ast.BinaryOperator
import kotlin.test.Test
import kotlin.test.assertEquals

class SymbolsTest {
    @Test
    fun `cada operador binario se escribe con su signo`() {
        assertEquals("+", BinaryOperator.PLUS.symbol())
        assertEquals("-", BinaryOperator.MINUS.symbol())
        assertEquals("*", BinaryOperator.TIMES.symbol())
        assertEquals("/", BinaryOperator.DIVIDE.symbol())
    }
}
