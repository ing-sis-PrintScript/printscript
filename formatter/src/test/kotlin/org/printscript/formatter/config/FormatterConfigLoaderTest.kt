package org.printscript.formatter.config

import org.printscript.common.Result
import org.printscript.common.errorOrNull
import org.printscript.common.getOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FormatterConfigLoaderTest {
    private val loader = FormatterConfigLoader()

    private fun loadOne(
        key: String,
        value: ConfigValue,
    ): Result<FormatterConfig, ConfigError> = loader.load(mapOf(key to value))

    private fun bool(enabled: Boolean): ConfigValue = ConfigValue.BooleanValue(enabled)

    private fun int(count: Int): ConfigValue = ConfigValue.IntValue(count)

    @Test
    fun `un mapa vacio devuelve la configuracion por defecto`() {
        assertEquals(FormatterConfig(), loader.load(emptyMap()).getOrNull())
    }

    @Test
    fun `el espacio antes de los dos puntos sale del booleano`() {
        val key = "enforce-spacing-before-colon-in-declaration"

        assertEquals(Spacing.SINGLE, loadOne(key, bool(true)).getOrNull()?.spaceBeforeColon)
        assertEquals(Spacing.NONE, loadOne(key, bool(false)).getOrNull()?.spaceBeforeColon)
    }

    @Test
    fun `el espacio despues de los dos puntos sale del booleano`() {
        val key = "enforce-spacing-after-colon-in-declaration"

        assertEquals(Spacing.SINGLE, loadOne(key, bool(true)).getOrNull()?.spaceAfterColon)
        assertEquals(Spacing.NONE, loadOne(key, bool(false)).getOrNull()?.spaceAfterColon)
    }

    @Test
    fun `el espacio alrededor del igual sale del booleano`() {
        val key = "enforce-spacing-around-equals"

        assertEquals(Spacing.SINGLE, loadOne(key, bool(true)).getOrNull()?.spaceAroundAssignment)
        assertEquals(Spacing.NONE, loadOne(key, bool(false)).getOrNull()?.spaceAroundAssignment)
    }

    @Test
    fun `las lineas en blanco antes del println salen del entero`() {
        val key = "line-breaks-after-println"

        assertEquals(BlankLines.TWO, loadOne(key, int(2)).getOrNull()?.blankLinesBeforePrintln)
        assertEquals(BlankLines.NONE, loadOne(key, int(0)).getOrNull()?.blankLinesBeforePrintln)
    }

    @Test
    fun `una clave que no corresponde a ninguna regla falla nombrandola`() {
        val error = loadOne("enforce-spacing-around-plus", bool(true)).errorOrNull()

        assertEquals(ConfigError.UnknownRule("enforce-spacing-around-plus"), error)
    }

    @Test
    fun `un booleano donde se esperaba un entero falla por tipo`() {
        val key = "line-breaks-after-println"

        assertEquals(ConfigError.WrongType(key, "int"), loadOne(key, bool(true)).errorOrNull())
    }

    @Test
    fun `un entero donde se esperaba un booleano falla por tipo`() {
        val key = "enforce-spacing-around-equals"

        assertEquals(ConfigError.WrongType(key, "boolean"), loadOne(key, int(1)).errorOrNull())
    }

    @Test
    fun `un entero fuera del rango permitido falla informando el rango`() {
        val key = "line-breaks-after-println"
        val error = loadOne(key, int(3)).errorOrNull()

        assertIs<ConfigError.OutOfRange>(error)
        assertEquals(BlankLines.ALLOWED, error.allowed)
        assertEquals(3, error.value)
    }

    @Test
    fun `con dos errores presentes se devuelve el primero`() {
        val values =
            mapOf(
                "clave-inventada" to bool(true),
                "line-breaks-after-println" to int(3),
            )

        assertEquals(ConfigError.UnknownRule("clave-inventada"), loader.load(values).errorOrNull())
    }

    @Test
    fun `las claves ausentes conservan su valor por defecto`() {
        val config = loadOne("enforce-spacing-around-equals", bool(false)).getOrNull()

        assertEquals(Spacing.NONE, config?.spaceAroundAssignment)
        assertEquals(FormatterConfig().spaceAfterColon, config?.spaceAfterColon)
        assertEquals(FormatterConfig().blankLinesBeforePrintln, config?.blankLinesBeforePrintln)
    }
}
