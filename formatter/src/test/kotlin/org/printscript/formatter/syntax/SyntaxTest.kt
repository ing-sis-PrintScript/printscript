package org.printscript.formatter.syntax

import org.printscript.ast.BinaryOperator
import org.printscript.ast.DeclaredType
import kotlin.test.Test
import kotlin.test.assertEquals

class SyntaxTest {
    @Test
    fun `cada operador binario se escribe con su signo`() {
        assertEquals("+", BinaryOperator.PLUS.symbol())
        assertEquals("-", BinaryOperator.MINUS.symbol())
        assertEquals("*", BinaryOperator.TIMES.symbol())
        assertEquals("/", BinaryOperator.DIVIDE.symbol())
    }

    @Test
    fun `cada tipo declarado se escribe en minuscula`() {
        assertEquals("number", DeclaredType.NUMBER.symbol())
        assertEquals("string", DeclaredType.STRING.symbol())
    }
}
