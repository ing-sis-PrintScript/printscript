package org.printscript.interpreter.statements

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.CallExpression
import org.printscript.ast.Expression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.io.PrintScriptIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExpressionStatementExecutorTest {
    private val dummyRange = Range(Position(1, 1), Position(1, 10))
    private val executor = ExpressionStatementExecutor()

    private class RecordingIO : PrintScriptIO {
        val printed = mutableListOf<String>()

        override fun print(message: String) {
            printed.add(message)
        }

        override fun read(prompt: String): String = ""
    }

    private fun errorOf(result: Result<Environment, InterpreterError>?): InterpreterError {
        assertIs<Result.Failure<InterpreterError>>(result, "esperaba Failure y vino Success o null")
        return result.error
    }

    private fun number(value: Double) = NumberLiteral(value, dummyRange)

    private fun binary(
        operator: BinaryOperator,
        left: Double,
        right: Double,
    ) = BinaryExpression(operator, number(left), number(right), dummyRange)

    private fun callTo(
        name: String,
        argument: Expression,
    ) = CallExpression(Identifier(name, dummyRange), listOf(argument), dummyRange)

    @Test
    fun `devuelve null si el statement no es un ExpressionStatement`() {
        val notAnExpressionStatement = AssignmentStatement(Identifier("x", dummyRange), number(1.0), dummyRange)

        assertEquals(null, executor.execute(notAnExpressionStatement, Environment(), RecordingIO()))
    }

    @Test
    fun `println imprime el valor evaluado del argumento`() {
        val io = RecordingIO()
        val statement = ExpressionStatement(callTo("println", number(5.0)), dummyRange)

        val result = executor.execute(statement, Environment(), io)

        assertIs<Result.Success<Environment>>(result)
        assertEquals(listOf("5"), io.printed)
    }

    @Test
    fun `println de una expresion compuesta evalua antes de imprimir`() {
        val io = RecordingIO()
        val call = callTo("println", binary(BinaryOperator.PLUS, 2.0, 3.0))

        executor.execute(ExpressionStatement(call, dummyRange), Environment(), io)

        assertEquals(listOf("5"), io.printed)
    }

    @Test
    fun `llamar a una funcion desconocida falla sin tocar la IO`() {
        val io = RecordingIO()
        val call = callTo("saludar", number(1.0))

        val error = errorOf(executor.execute(ExpressionStatement(call, dummyRange), Environment(), io))

        assertEquals("No existe la función 'saludar'.", error.message)
        assertEquals(emptyList(), io.printed)
    }

    @Test
    fun `println propaga el error si el argumento falla al evaluarse`() {
        val io = RecordingIO()
        val call = callTo("println", Identifier("noExiste", dummyRange))

        val error = errorOf(executor.execute(ExpressionStatement(call, dummyRange), Environment(), io))

        assertEquals("La variable 'noExiste' no ha sido declarada.", error.message)
        assertEquals(emptyList(), io.printed)
    }

    @Test
    fun `una expresion suelta que no es una llamada se evalua y se descarta sin imprimir nada`() {
        val io = RecordingIO()
        val sum = binary(BinaryOperator.PLUS, 2.0, 3.0)

        val result = executor.execute(ExpressionStatement(sum, dummyRange), Environment(), io)

        assertIs<Result.Success<Environment>>(result)
        assertEquals(emptyList(), io.printed)
    }

    @Test
    fun `una expresion suelta invalida sigue fallando aunque no sea una llamada`() {
        val io = RecordingIO()
        val division = binary(BinaryOperator.DIVIDE, 1.0, 0.0)

        val error = errorOf(executor.execute(ExpressionStatement(division, dummyRange), Environment(), io))

        assertEquals("División por cero.", error.message)
    }
}
