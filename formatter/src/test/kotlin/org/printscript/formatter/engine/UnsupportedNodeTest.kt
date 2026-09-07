package org.printscript.formatter.engine

import org.printscript.ast.BinaryOperator.PLUS
import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.formatter.expressions.binary
import org.printscript.formatter.expressions.id
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnsupportedNodeTest {
    private val node = binary(PLUS, id("a"), id("b"))

    @Test
    fun `el mensaje nombra el tipo de nodo que no se pudo formatear`() {
        assertEquals("Esta versión de PrintScript no formatea BinaryExpression", UnsupportedNode(node).message)
    }

    @Test
    fun `el error apunta al rango del nodo`() {
        val range = Range(Position(3, 5), Position(3, 12))

        assertEquals(range, UnsupportedNode(node.copy(range = range)).range)
    }

    @Test
    fun `se describe con el rango y el mensaje`() {
        val error = UnsupportedNode(node)

        assertTrue(error.toString().startsWith("Error de formato en ${node.range}"))
        assertTrue(error.toString().endsWith(error.message))
    }
}
