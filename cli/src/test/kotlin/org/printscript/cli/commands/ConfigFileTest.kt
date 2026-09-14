package org.printscript.cli.commands

import org.printscript.common.errorOrNull
import org.printscript.common.getOrNull
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConfigFileTest {
    private fun archivo(
        nombre: String,
        contenido: String,
    ): File {
        val file = File.createTempFile(nombre.substringBeforeLast('.'), "." + nombre.substringAfterLast('.'))
        file.deleteOnExit()
        file.writeText(contenido)
        return file
    }

    @Test
    fun `una extension desconocida es un error`() {
        val error = assertNotNull(configText(archivo("formato.txt", "")).errorOrNull())

        assertTrue(error.message.contains(".yaml"))
    }

    @Test
    fun `las tres extensiones validas devuelven el contenido`() {
        for (extension in listOf("yaml", "yml", "json")) {
            assertEquals("hola", configText(archivo("formato.$extension", "hola")).getOrNull())
        }
    }

    @Test
    fun `la extension se compara sin importar mayusculas`() {
        assertEquals("hola", configText(archivo("formato.YAML", "hola")).getOrNull())
    }
}
