package org.printscript.interpreter

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.CallExpression
import org.printscript.ast.DeclaredType
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.ast.VariableDeclaration
import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.interpreter.io.PrintScriptIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InterpreterTest {
    private val dummyRange = Range(Position(1, 1), Position(1, 10))

    class MockIO : PrintScriptIO {
        val outputs = mutableListOf<String>()

        override fun print(message: String) {
            outputs.add(message)
        }

        override fun read(prompt: String): String = ""
    }

    @Test
    fun shouldResolveMathTest() {
        val mockIO = MockIO()
        val interpreter: PrintScriptInterpreter = Interpreter(io = mockIO)

        interpreter.execute(
            VariableDeclaration(
                identifier = Identifier("a", dummyRange),
                declaredType = DeclaredType.NUMBER,
                initializer = NumberLiteral(10.0, dummyRange),
                range = dummyRange,
            ),
        )

        interpreter.execute(
            VariableDeclaration(
                identifier = Identifier("b", dummyRange),
                declaredType = DeclaredType.NUMBER,
                initializer = NumberLiteral(2.0, dummyRange),
                range = dummyRange,
            ),
        )

        val division =
            BinaryExpression(
                operator = BinaryOperator.DIVIDE,
                left = Identifier("a", dummyRange),
                right = Identifier("b", dummyRange),
                range = dummyRange,
            )
        val concat =
            BinaryExpression(
                operator = BinaryOperator.PLUS,
                left = StringLiteral("Resultado: ", dummyRange),
                right = division,
                range = dummyRange,
            )
        val printlnCall =
            ExpressionStatement(
                expression =
                    CallExpression(
                        callee = Identifier("println", dummyRange),
                        arguments = listOf(concat),
                        range = dummyRange,
                    ),
                range = dummyRange,
            )

        val result = interpreter.execute(printlnCall)

        assertTrue(result is Result.Success, "La ejecución debería ser un éxito")
        assertEquals(listOf("Resultado: 5"), mockIO.outputs)
    }

    @Test
    fun divisionByZeroTest() {
        val interpreter: PrintScriptInterpreter = Interpreter(io = MockIO())

        val division =
            BinaryExpression(
                operator = BinaryOperator.DIVIDE,
                left = NumberLiteral(10.0, dummyRange),
                right = NumberLiteral(0.0, dummyRange),
                range = dummyRange,
            )
        val printlnCall =
            ExpressionStatement(
                expression =
                    CallExpression(
                        callee = Identifier("println", dummyRange),
                        arguments = listOf(division),
                        range = dummyRange,
                    ),
                range = dummyRange,
            )

        val result = interpreter.execute(printlnCall)

        assertTrue(result is Result.Failure, "Debería haber fallado")
        val error = (result as Result.Failure).error
        assertEquals("División por cero.", error.message)
    }

    @Test
    fun `declarar sin inicializador y despues asignar deja la variable lista para leerse`() {
        val mockIO = MockIO()
        val interpreter: PrintScriptInterpreter = Interpreter(io = mockIO)

        interpreter.execute(
            VariableDeclaration(
                identifier = Identifier("a", dummyRange),
                declaredType = DeclaredType.STRING,
                initializer = null,
                range = dummyRange,
            ),
        )
        interpreter.execute(
            AssignmentStatement(
                target = Identifier("a", dummyRange),
                value = StringLiteral("hola", dummyRange),
                range = dummyRange,
            ),
        )

        val result =
            interpreter.execute(
                ExpressionStatement(
                    expression =
                        CallExpression(
                            callee = Identifier("println", dummyRange),
                            arguments = listOf(Identifier("a", dummyRange)),
                            range = dummyRange,
                        ),
                    range = dummyRange,
                ),
            )

        assertTrue(result is Result.Success, "La ejecución debería ser un éxito")
        assertEquals(listOf("hola"), mockIO.outputs)
    }

    @Test
    fun `redeclarar la misma variable propaga el error de Environment a traves del Interpreter`() {
        val interpreter: PrintScriptInterpreter = Interpreter(io = MockIO())
        val declaration =
            VariableDeclaration(
                identifier = Identifier("a", dummyRange),
                declaredType = DeclaredType.NUMBER,
                initializer = NumberLiteral(1.0, dummyRange),
                range = dummyRange,
            )

        interpreter.execute(declaration)
        val result = interpreter.execute(declaration)

        assertIs<Result.Failure<InterpreterError>>(result)
        assertEquals("La variable 'a' ya fue declarada.", result.error.message)
    }

    @Test
    fun `asignar a una variable no declarada propaga el error de Environment a traves del Interpreter`() {
        val interpreter: PrintScriptInterpreter = Interpreter(io = MockIO())

        val result =
            interpreter.execute(
                AssignmentStatement(
                    target = Identifier("noExiste", dummyRange),
                    value = NumberLiteral(1.0, dummyRange),
                    range = dummyRange,
                ),
            )

        assertIs<Result.Failure<InterpreterError>>(result)
        assertEquals("La variable 'noExiste' no ha sido declarada.", result.error.message)
    }

    @Test
    fun `llamar a una funcion desconocida propaga el error a traves del Interpreter`() {
        val interpreter: PrintScriptInterpreter = Interpreter(io = MockIO())

        val result =
            interpreter.execute(
                ExpressionStatement(
                    expression =
                        CallExpression(
                            callee = Identifier("saludar", dummyRange),
                            arguments = listOf(NumberLiteral(1.0, dummyRange)),
                            range = dummyRange,
                        ),
                    range = dummyRange,
                ),
            )

        assertIs<Result.Failure<InterpreterError>>(result)
        assertEquals("No existe la función 'saludar'.", result.error.message)
    }
}
