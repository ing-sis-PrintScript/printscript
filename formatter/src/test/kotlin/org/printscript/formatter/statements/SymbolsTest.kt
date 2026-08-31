package org.printscript.formatter.statements

import org.printscript.ast.DeclaredType
import kotlin.test.Test
import kotlin.test.assertEquals

class SymbolsTest {
    @Test
    fun `cada tipo declarado se escribe en minuscula`() {
        assertEquals("number", DeclaredType.NUMBER.symbol())
        assertEquals("string", DeclaredType.STRING.symbol())
    }
}
