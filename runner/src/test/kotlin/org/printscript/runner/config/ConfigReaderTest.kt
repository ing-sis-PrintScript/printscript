package org.printscript.runner.config

import org.printscript.common.errorOrNull
import org.printscript.common.flatMap
import org.printscript.common.getOrNull
import org.printscript.formatter.config.ConfigValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConfigReaderTest {
    // Leer y tipar, que antes hacia ConfigReader de una y ahora son dos pasos.
    private fun leer(text: String) = ConfigReader().readTree(text).flatMap { root -> toConfigValues(root) }

    @Test
    fun `lee un yaml`() {
        val values =
            leer(
                """
                enforce-spacing-around-equals: true
                line-breaks-after-println: 2
                """.trimIndent(),
            ).getOrNull()

        assertEquals(
            mapOf(
                "enforce-spacing-around-equals" to ConfigValue.BooleanValue(true),
                "line-breaks-after-println" to ConfigValue.IntValue(2),
            ),
            values,
        )
    }

    @Test
    fun `lee un json y da lo mismo que el yaml equivalente`() {
        val json = leer("""{ "enforce-spacing-around-equals": true }""").getOrNull()
        val yaml = leer("enforce-spacing-around-equals: true").getOrNull()

        assertEquals(yaml, json)
    }

    @Test
    fun `un yaml mal formado es un error, no una excepcion`() {
        val error = assertNotNull(leer("esto: [ no cierra").errorOrNull())

        assertTrue(error.message.contains("no se pudo leer"))
    }

    @Test
    fun `un valor que no es booleano ni entero es un error`() {
        val error = assertNotNull(leer("line-breaks-after-println: hola").errorOrNull())

        assertTrue(error.message.contains("line-breaks-after-println"))
    }

    @Test
    fun `un archivo vacio da una config vacia`() {
        assertEquals(emptyMap(), leer("").getOrNull())
    }

    @Test
    fun `un archivo con solo comentarios tambien`() {
        assertEquals(emptyMap(), leer("# todavia no configure nada").getOrNull())
    }
}
