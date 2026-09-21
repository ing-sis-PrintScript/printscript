package org.printscript.formatter.versions

import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.BracePosition
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Indent
import org.printscript.lexer.versions.lexerFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Lo mismo que GoldenFilesTest pero para las reglas que trae 1.1, y con el lexer de
 * 1.1: sin él, "if" y las llaves ni siquiera se tokenizan.
 */
class PrintScript11GoldenTest {
    private fun recurso(path: String): String =
        checkNotNull(javaClass.getResourceAsStream(path)) { "falta el recurso $path" }
            .bufferedReader()
            .readLines()
            .joinToString("\n")

    private fun formatear(
        fuente: String,
        config: FormatterConfig,
    ): String =
        PrintScript11.formatter(config)
            .format(lexerFor(Version.V11).tokenize(fuente))
            .joinToString("") { result ->
                when (result) {
                    is Result.Success -> result.value.text
                    is Result.Failure -> fail("no esperaba un error: ${result.error.message}")
                }
            }

    private fun verificar(
        fuente: String,
        golden: String,
        config: FormatterConfig,
    ) = assertEquals(recurso("/golden/$golden"), formatear(recurso("/source/$fuente"), config))

    @Test
    fun `if-brace-same-line sube la llave al renglon del if`() {
        verificar("if-llave-abajo.ps", "if-llave-arriba.ps", FormatterConfig(ifBrace = BracePosition.SAME_LINE))
    }

    @Test
    fun `if-brace-below-line baja la llave a su propio renglon`() {
        verificar("if-llave-arriba.ps", "if-llave-abajo.ps", FormatterConfig(ifBrace = BracePosition.BELOW_LINE))
    }

    // La clave de la llave no arrastra a la sangria: el cuerpo conserva sus 2 espacios
    // aunque la llave se mueva. Es lo que piden los goldens del TCK.
    @Test
    fun `mover la llave no toca la sangria de adentro`() {
        verificar("if-anidado.ps", "if-anidado.ps", FormatterConfig(ifBrace = BracePosition.SAME_LINE))
    }

    @Test
    fun `indent-inside-if sangra por nivel de bloque`() {
        verificar("if-anidado.ps", "if-anidado-sangria-4.ps", FormatterConfig(indentInsideIf = Indent.of(4)))
    }

    // Sin la clave, la sangria del fuente se conserva tal cual.
    @Test
    fun `sin indent-inside-if la sangria del fuente queda igual`() {
        verificar("if-anidado.ps", "if-anidado.ps", FormatterConfig())
    }

    // El caso que justifica que la sangria NO sea una regla: si compitiera con
    // line-breaks-after-println, una de las dos perderia. Acá se componen — la regla
    // pone los saltos y la sangria completa el ultimo renglon.
    @Test
    fun `la sangria se compone con las lineas en blanco despues de un println`() {
        verificar(
            "if-dos-println.ps",
            "if-dos-println-sangria-4-y-linea.ps",
            FormatterConfig(indentInsideIf = Indent.of(4), lineBreaksAfterPrintln = BlankLines.ONE),
        )
    }

    @Test
    fun `un bloque en una linea se abre en renglones`() {
        verificar(
            "bloque-en-una-linea.ps",
            "bloque-en-una-linea-saltos.ps",
            FormatterConfig(
                lineBreakAfterStatement = true,
                ifBrace = BracePosition.SAME_LINE,
                indentInsideIf = Indent.of(4),
            ),
        )
    }

    @Test
    fun `el else queda pegado a la llave que cierra el if`() {
        verificar(
            "if-else-en-una-linea.ps",
            "if-else-saltos.ps",
            FormatterConfig(
                lineBreakAfterStatement = true,
                ifBrace = BracePosition.SAME_LINE,
                indentInsideIf = Indent.of(4),
            ),
        )
    }

    @Test
    fun `las llaves cortan la linea en blanco de un println anterior`() {
        verificar(
            "println-antes-de-if.ps",
            "println-antes-de-if-sangria-4-y-linea.ps",
            FormatterConfig(indentInsideIf = Indent.of(4), lineBreaksAfterPrintln = BlankLines.ONE),
        )
    }

    @Test
    fun `sin indent-inside-if el salto obligatorio conserva la sangria del fuente`() {
        verificar(
            "if-sangria-del-fuente.ps",
            "if-sangria-del-fuente.ps",
            FormatterConfig(lineBreakAfterStatement = true),
        )
    }

    @Test
    fun `los ifs anidados en una linea se abren con sangria por nivel`() {
        verificar(
            "if-anidado-en-una-linea.ps",
            "if-anidado-saltos.ps",
            FormatterConfig(
                lineBreakAfterStatement = true,
                ifBrace = BracePosition.SAME_LINE,
                indentInsideIf = Indent.of(4),
            ),
        )
    }
}
