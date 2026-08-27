package org.printscript.formatter

import org.printscript.ast.DeclaredType.NUMBER
import org.printscript.formatter.expressions.call
import org.printscript.formatter.expressions.id
import org.printscript.formatter.expressions.number
import org.printscript.formatter.statements.declaration
import org.printscript.formatter.statements.expressionStatement
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrintlnRecognizerTest {
    private val recognizer = PrintlnRecognizer()

    @Test
    fun `una sentencia de expresion que llama a println es reconocida`() {
        assertTrue(recognizer.matches(expressionStatement(call("println", id("x")))))
    }

    @Test
    fun `una llamada a otra funcion no es reconocida`() {
        assertFalse(recognizer.matches(expressionStatement(call("print", id("x")))))
    }

    @Test
    fun `una sentencia de expresion que no es una llamada no es reconocida`() {
        assertFalse(recognizer.matches(expressionStatement(id("x"))))
    }

    @Test
    fun `una declaracion no es reconocida`() {
        assertFalse(recognizer.matches(declaration("x", NUMBER, number(5.0))))
    }

    @Test
    fun `una llamada suelta sin envolver en una sentencia no es reconocida`() {
        assertFalse(recognizer.matches(call("println", id("x"))))
    }
}
