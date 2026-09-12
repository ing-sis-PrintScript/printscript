package org.printscript.runner

import org.printscript.common.PrintScriptError
import org.printscript.common.Version
import org.printscript.common.errorOrNull
import org.printscript.lexer.source.StringSourceReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionPipelineTest {
    private fun parsear(
        fuente: String,
        version: Version,
    ) = statements({ StringSourceReader(fuente) }, version).toList()

    private fun errores(
        fuente: String,
        version: Version,
    ): List<PrintScriptError> = parsear(fuente, version).mapNotNull { it.errorOrNull() }

    @Test
    fun `un programa de 1_0 anda igual en las dos versiones`() {
        val fuente = """let name: string = "Joe";println(name);"""

        assertEquals(parsear(fuente, Version.V10), parsear(fuente, Version.V11))
    }

    @Test
    fun `un programa de 1_0 no tiene errores en ninguna de las dos`() {
        val fuente = "let x: number = 5;"

        assertTrue(errores(fuente, Version.V10).isEmpty())
        assertTrue(errores(fuente, Version.V11).isEmpty())
    }

    // Lo que pide la consigna: usar algo que no existe en la version elegida tiene que
    // dar error. No hay un chequeo de version en ningun lado -- pasa solo, porque con
    // las reglas de 1.0 la palabra 'const' no es keyword y sale IDENTIFIER.
    @Test
    fun `un programa de 1_1 corrido como 1_0 falla`() {
        val fuente = """const activo: boolean = true;"""

        assertTrue(errores(fuente, Version.V10).isNotEmpty())
    }

    // El mismo archivo es valido en una version e invalido en la otra. Esto es lo que
    // garantiza que --version 1.0 no acepte un programa de 1.1.
    @Test
    fun `la misma palabra se lee distinto segun la version`() {
        val fuente = """const activo: boolean = true;"""

        assertTrue(errores(fuente, Version.V11).isEmpty(), "como 1.1 tiene que ser valido")

        // Como 1.0, 'const' no esta en el mapa de keywords: sale IDENTIFIER, y como una
        // asignacion empieza con un identificador, AssignmentParser lo agarra creyendo
        // que es el nombre de una variable. Por eso se queja del '=' que no aparece.
        val comoDiezCero = errores(fuente, Version.V10).first().message

        assertTrue(comoDiezCero.contains("'='"), "como 1.0: $comoDiezCero")
    }
}
