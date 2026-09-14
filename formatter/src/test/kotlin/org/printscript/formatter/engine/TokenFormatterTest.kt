package org.printscript.formatter.engine

import org.printscript.common.Result
import org.printscript.formatter.config.BlankLines
import org.printscript.formatter.config.Spacing
import org.printscript.formatter.rules.LineBreakAfterStatementRule
import org.printscript.formatter.rules.LineBreaksAfterPrintlnRule
import org.printscript.formatter.rules.SingleSpaceSeparationRule
import org.printscript.formatter.rules.SpacingMatcher
import org.printscript.formatter.rules.colonSpacing
import org.printscript.formatter.rules.operatorSpacing
import org.printscript.lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.fail

class TokenFormatterTest {
    private fun formatear(
        fuente: String,
        matcher: SpacingMatcher = SpacingMatcher(),
    ): String =
        TokenFormatter(matcher).format(Lexer().tokenize(fuente))
            .joinToString("") { result ->
                when (result) {
                    is Result.Success -> result.value.text
                    is Result.Failure -> fail("no esperaba un error lexico: ${result.error.message}")
                }
            }

    // El test que justifica todo el diseño: sin reglas activas, formatear no toca nada.
    private fun identidad(fuente: String) = assertEquals(fuente, formatear(fuente))

    @Test
    fun `una declaracion sale igual que como entro`() {
        identidad("let x: number = 5;")
    }

    @Test
    fun `el espaciado raro se conserva tal cual`() {
        identidad("let  x   :   number=5;")
    }

    @Test
    fun `los tabs se conservan como tabs`() {
        identidad("let x\t: number = 5;")
    }

    @Test
    fun `las lineas en blanco y la indentacion se conservan`() {
        identidad("let a: number = 1;\n\n    println(a);")
    }

    @Test
    fun `dos declaraciones con espaciados distintos salen cada una como venia`() {
        identidad("let a:string = \"x\";\nlet b: string = \"y\";")
    }

    @Test
    fun `un string con comillas dobles vuelve entero`() {
        identidad("let a: string = \"Joe Doe\";")
    }

    // Limitacion conocida, no un bug suelto: el token no guarda que comilla traia el
    // fuente, asi que el formatter escribe siempre la doble. Ningun .ps del TCK usa
    // comilla simple.
    @Test
    fun `una comilla simple sale convertida en doble`() {
        assertEquals("let a: string = \"Joe\";", formatear("let a: string = 'Joe';"))
    }

    // El salto final CIERRA la ultima linea, no abre una vacia, y el EOF va sin trivia.
    // Que el archivo termine o no en salto lo decide una regla, no el recorrido.
    @Test
    fun `el salto final del archivo no se conserva`() {
        assertEquals("let a: number = 1;", formatear("let a: number = 1;\n"))
    }

    @Test
    fun `la regla del colon reescribe ese espacio y no toca el resto`() {
        val matcher = SpacingMatcher(listOf(colonSpacing(Spacing.NONE, null)))

        assertEquals("let  x: number   = 5;", formatear("let  x : number   = 5;", matcher))
    }

    @Test
    fun `la regla del colon tambien ordena el espacio de atras`() {
        val matcher = SpacingMatcher(listOf(colonSpacing(Spacing.NONE, Spacing.SINGLE)))

        assertEquals("let x: string = \"a\";", formatear("let x   :string = \"a\";", matcher))
    }

    // El caso de los goldens: una regla activa normaliza SU espacio en las dos
    // declaraciones, y todo lo demas de cada una sale como venia.
    @Test
    fun `dos declaraciones distintas conservan lo suyo salvo donde la regla manda`() {
        val matcher = SpacingMatcher(listOf(colonSpacing(Spacing.NONE, null)))

        assertEquals(
            "let a:string = \"x\";\n\nlet  b: string   = \"y\";",
            formatear("let a:string = \"x\";\n\nlet  b : string   = \"y\";", matcher),
        )
    }

    // Los dos casos del TCK, con los main y golden de print-0 y print-1.
    @Test
    fun `line-breaks-after-println en cero borra las lineas en blanco de mas`() {
        val matcher = SpacingMatcher(listOf(LineBreaksAfterPrintlnRule(BlankLines.NONE)))
        val main = "let a:string = \"x\";\nprintln(a);\n\n\n\n\nprintln(\"y\");"

        assertEquals("let a:string = \"x\";\nprintln(a);\nprintln(\"y\");", formatear(main, matcher))
    }

    @Test
    fun `line-breaks-after-println en uno separa los println y no toca el let`() {
        val matcher = SpacingMatcher(listOf(LineBreaksAfterPrintlnRule(BlankLines.ONE)))
        val main = "let a:string = \"x\";\nprintln(a);\nprintln(\"y\");"

        assertEquals("let a:string = \"x\";\nprintln(a);\n\nprintln(\"y\");", formatear(main, matcher))
    }

    // Los main y golden de mandatory-line-break-after-statement: mete los saltos y no
    // toca el espaciado propio de cada declaracion.
    @Test
    fun `el salto obligatorio despues del punto y coma no toca nada mas`() {
        val matcher = SpacingMatcher(listOf(LineBreakAfterStatementRule(mandatory = true)))
        val main = "let a:string = \"x\";let b : string = \"y\";let c :string=\"z\";"

        assertEquals("let a:string = \"x\";\nlet b : string = \"y\";\nlet c :string=\"z\";", formatear(main, matcher))
    }

    @Test
    fun `los operadores quedan rodeados de espacios`() {
        val matcher = SpacingMatcher(listOf(operatorSpacing(mandatory = true)))

        assertEquals("let r: number = 5 + 4 * 3 / 2;", formatear("let r: number = 5+4*3/2;", matcher))
    }

    // El golden de mandatory-single-space-separation: un espacio entre todo, salvo antes
    // del ';', y sin juntar los dos statements en un renglon.
    @Test
    fun `la separacion por un espacio toca todo menos el punto y coma y los saltos`() {
        val matcher = SpacingMatcher(listOf(SingleSpaceSeparationRule(mandatory = true)))
        val main = "let something:      string=\"a really cool thing\";\nprintln(something);"

        assertEquals(
            "let something : string = \"a really cool thing\";\nprintln ( something );",
            formatear(main, matcher),
        )
    }

    @Test
    fun `un error lexico corta la secuencia`() {
        val resultados = TokenFormatter().format(Lexer().tokenize("let a = 1;\n@\nlet b = 2;")).toList()

        assertIs<Result.Failure<*>>(resultados.last())
        assertEquals(1, resultados.count { it is Result.Failure })
    }

    @Test
    fun `la secuencia se recorre una sola vez`() {
        val formateado = TokenFormatter().format(Lexer().tokenize("let x = 5;"))

        formateado.toList()

        assertFailsWith<IllegalArgumentException> { formateado.toList() }
    }
}
