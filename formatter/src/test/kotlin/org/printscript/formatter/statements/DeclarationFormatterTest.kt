package org.printscript.formatter.statements

import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.ast.DeclaredType.NUMBER
import org.printscript.ast.DeclaredType.STRING
import org.printscript.ast.VariableDeclaration
import org.printscript.formatter.FormatterContext
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.Spacing
import org.printscript.formatter.expressions.PrintScript10ExpressionFormatter
import org.printscript.formatter.expressions.binary
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import org.printscript.formatter.expressions.string
import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationFormatterTest {
    private val formatter = DeclarationFormatter(PrintScript10ExpressionFormatter())

    private fun format(
        node: VariableDeclaration,
        config: FormatterConfig = FormatterConfig(),
    ): String = formatter.format(node, FormatterContext(config)).text

    private val declaration = declaration("x", NUMBER, number(5.0))

    @Test
    fun `una declaracion con inicializador usa la config por defecto`() {
        assertEquals("let x: number = 5;", format(declaration))
    }

    @Test
    fun `una declaracion sin inicializador termina despues del tipo`() {
        assertEquals("let x: number;", format(declaration("x", NUMBER, null)))
    }

    @Test
    fun `una declaracion de texto usa el simbolo string`() {
        assertEquals("let name: string = \"Joe\";", format(declaration("name", STRING, string("Joe"))))
    }

    @Test
    fun `con espacio antes de los dos puntos`() {
        val config = FormatterConfig(spaceBeforeColon = Spacing.SINGLE)

        assertEquals("let x : number = 5;", format(declaration, config))
    }

    @Test
    fun `sin espacio despues de los dos puntos`() {
        val config = FormatterConfig(spaceAfterColon = Spacing.NONE)

        assertEquals("let x:number = 5;", format(declaration, config))
    }

    @Test
    fun `sin espacio alrededor del igual`() {
        val config = FormatterConfig(spaceAroundAssignment = Spacing.NONE)

        assertEquals("let x: number=5;", format(declaration, config))
    }

    @Test
    fun `con las tres reglas de espaciado apagadas`() {
        val config =
            FormatterConfig(
                spaceBeforeColon = Spacing.NONE,
                spaceAfterColon = Spacing.NONE,
                spaceAroundAssignment = Spacing.NONE,
            )

        assertEquals("let x:number=5;", format(declaration, config))
    }

    @Test
    fun `el inicializador se delega al formatter de expresiones`() {
        val initializer = binary(PLUS, number(1.0), id("b"))

        assertEquals("let x: number = 1 + b;", format(declaration("x", NUMBER, initializer)))
    }
}
