package org.printscript.analyzer.config

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NamingConventionTest {
    @Test
    fun `CamelCase acepta camelCase y rechaza snake_case`() {
        assertTrue(CamelCase.matches("miVariable"))
        assertTrue(CamelCase.matches("x"))
        assertFalse(CamelCase.matches("mi_variable"))
        assertFalse(CamelCase.matches("MiVariable"))
    }

    @Test
    fun `SnakeCase acepta snake_case y rechaza camelCase`() {
        assertTrue(SnakeCase.matches("mi_variable"))
        assertTrue(SnakeCase.matches("x"))
        assertFalse(SnakeCase.matches("miVariable"))
        assertFalse(SnakeCase.matches("Mi_variable"))
    }
}
