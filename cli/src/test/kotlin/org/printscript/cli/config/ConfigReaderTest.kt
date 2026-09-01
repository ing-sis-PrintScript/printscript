package org.printscript.cli.config

import org.printscript.common.errorOrNull
import org.printscript.common.getOrNull
import org.printscript.formatter.config.ConfigValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConfigReaderTest {
    private val reader = ConfigReader()

    @Test
    fun `lee un yaml`() {
        val values =
            reader.read(
                "formato.yaml",
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
        val json = reader.read("formato.json", """{ "enforce-spacing-around-equals": true }""").getOrNull()
        val yaml = reader.read("formato.yaml", "enforce-spacing-around-equals: true").getOrNull()

        assertEquals(yaml, json)
    }

    @Test
    fun `una extension desconocida es un error`() {
        val error = assertNotNull(reader.read("formato.txt", "").errorOrNull())

        assertTrue(error.message.contains(".yaml"))
    }

    @Test
    fun `un yaml mal formado es un error, no una excepcion`() {
        val error = assertNotNull(reader.read("formato.yaml", "esto: [ no cierra").errorOrNull())

        assertTrue(error.message.contains("no se pudo leer"))
    }

    @Test
    fun `un valor que no es booleano ni entero es un error`() {
        val error = assertNotNull(reader.read("formato.yaml", "line-breaks-after-println: hola").errorOrNull())

        assertTrue(error.message.contains("line-breaks-after-println"))
    }

    @Test
    fun `un archivo vacio da una config vacia`() {
        assertEquals(emptyMap(), reader.read("formato.yaml", "").getOrNull())
    }

    @Test
    fun `un archivo con solo comentarios tambien`() {
        assertEquals(emptyMap(), reader.read("formato.yaml", "# todavia no configure nada").getOrNull())
    }
}
