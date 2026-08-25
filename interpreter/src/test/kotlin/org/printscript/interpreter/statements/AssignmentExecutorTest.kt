package org.printscript.interpreter.statements

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.DeclaredType
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.PrintScriptValue
import org.printscript.interpreter.io.PrintScriptIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AssignmentExecutorTest {
    private val dummyRange = Range(Position(1, 1), Position(1, 10))
    private val executor = AssignmentExecutor()

    private object NoOpIO : PrintScriptIO {
        override fun print(message: String) = Unit

        override fun read(prompt: String): String = ""
    }

    private fun declared(
        name: String,
        type: DeclaredType,
        value: PrintScriptValue,
    ): Environment = (Environment().declare(name, type, value, dummyRange) as Result.Success).value

    private fun assignment(
        name: String,
        value: Expression,
    ) = AssignmentStatement(Identifier(name, dummyRange), value, dummyRange)

    private fun valueOf(result: Result<Environment, InterpreterError>): Environment {
        assertIs<Result.Success<Environment>>(result, "esperaba Success y vino Failure")
        return result.value
    }

    private fun errorOf(result: Result<Environment, InterpreterError>): InterpreterError {
        assertIs<Result.Failure<InterpreterError>>(result, "esperaba Failure y vino Success")
        return result.error
    }

    @Test
    fun `solo maneja AssignmentStatement`() {
        assertEquals(true, executor.canHandle(assignment("x", NumberLiteral(1.0, dummyRange))))
    }

    @Test
    fun `reasigna una variable ya declarada`() {
        val env = declared("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(1.0))

        val result = valueOf(executor.execute(assignment("x", NumberLiteral(99.0, dummyRange)), env, NoOpIO))

        assertEquals(PrintScriptValue.NumberValue(99.0), (result.get("x", dummyRange) as Result.Success).value)
    }

    @Test
    fun `asignar a una variable no declarada falla`() {
        val error = errorOf(executor.execute(assignment("x", NumberLiteral(1.0, dummyRange)), Environment(), NoOpIO))

        assertEquals("La variable 'x' no ha sido declarada.", error.message)
    }

    @Test
    fun `asignar un tipo incompatible falla`() {
        val env = declared("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(1.0))

        val error =
            errorOf(
                executor.execute(assignment("x", StringLiteral("hola", dummyRange)), env, NoOpIO),
            )

        assertEquals("Se esperaba un tipo 'NUMBER' pero se obtuvo un valor distinto.", error.message)
    }

    @Test
    fun `propaga el error si el valor a asignar falla al evaluarse`() {
        val env = declared("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(1.0))

        val error =
            errorOf(executor.execute(assignment("x", Identifier("noExiste", dummyRange)), env, NoOpIO))

        assertEquals("La variable 'noExiste' no ha sido declarada.", error.message)
    }
}
