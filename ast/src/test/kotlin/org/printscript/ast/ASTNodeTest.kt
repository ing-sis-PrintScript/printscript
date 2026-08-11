package org.printscript.ast

import org.printscript.common.Position
import org.printscript.common.Range
import kotlin.test.Test
import kotlin.test.assertEquals

class ASTNodeTest {

    private fun range(line: Int, from: Int, to: Int) =
        Range(Position(line, from), Position(line, to))

    /** let name: string = "Joe"; */
    @Test
    fun `declaracion con inicializador`() {
        val declaration = VariableDeclaration(
            identifier = Identifier("name", range(1, 5, 8)),
            declaredType = DeclaredType.STRING,
            initializer = StringLiteral("Joe", range(1, 20, 24)),
            range = range(1, 1, 25),
        )

        assertEquals("name", declaration.identifier.name)
        assertEquals(DeclaredType.STRING, declaration.declaredType)
    }

    /** println(name + " "); */
    @Test
    fun `println con concatenacion`() {
        val statement = ExpressionStatement(
            expression = CallExpression(
                callee = Identifier("println", range(1, 1, 7)),
                arguments = listOf(
                    BinaryExpression(
                        operator = BinaryOperator.PLUS,
                        left = Identifier("name", range(1, 9, 12)),
                        right = StringLiteral(" ", range(1, 16, 18)),
                        range = range(1, 9, 18),
                    ),
                ),
                range = range(1, 1, 19),
            ),
            range = range(1, 1, 20),
        )

        val call = statement.expression as CallExpression
        assertEquals("println", call.callee.name)
        assertEquals(1, call.arguments.size)
    }
}